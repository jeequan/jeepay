/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.pay.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IDivisionService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.service.impl.PayOrderDivisionRecordService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/*
* 分账补单定时任务
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2023/3/29 11:35
*/
@Slf4j
@Component
public class PayOrderDivisionRecordReissueTask {

    private static final int QUERY_PAGE_SIZE = 100; //每次查询数量

    @Autowired private PayOrderDivisionRecordService payOrderDivisionRecordService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayOrderService payOrderService;

    @Scheduled(cron="0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        log.info("处理分账补单任务 开始");

        //当前时间 减去5分钟。
        Date offsetDate = DateUtil.offsetMinute(new Date(), -5);

        //查询条件： 受理中的订单 & （ 订单创建时间 + 5分钟 >= 当前时间 ）
        LambdaQueryWrapper<PayOrderDivisionRecord> lambdaQueryWrapper = PayOrderDivisionRecord.gw().
                eq(PayOrderDivisionRecord::getState, PayOrderDivisionRecord.STATE_ACCEPT).le(PayOrderDivisionRecord::getCreatedAt, offsetDate);

        int currentPageIndex = 1; //当前页码

        while(true){

            try {
                IPage<PayOrderDivisionRecord> pageRecordList = payOrderDivisionRecordService.getBaseMapper().distinctBatchOrderIdList(new Page(currentPageIndex, QUERY_PAGE_SIZE), lambdaQueryWrapper);

                log.info("处理分账补单任务, 共计{}条", pageRecordList.getTotal());

                //本次查询无结果, 不再继续查询;
                if(pageRecordList == null || pageRecordList.getRecords() == null || pageRecordList.getRecords().isEmpty()){
                    break;
                }

                for(PayOrderDivisionRecord batchRecord: pageRecordList.getRecords()){

                    try {
                        String batchOrderId = batchRecord.getBatchOrderId();

                        // 通过 batchId 查询出列表（ 注意：  需要按照ID 排序！！！！ ）
                        List<PayOrderDivisionRecord> recordList = payOrderDivisionRecordService.list(PayOrderDivisionRecord.gw()
                                .eq(PayOrderDivisionRecord::getState, PayOrderDivisionRecord.STATE_ACCEPT)
                                .eq(PayOrderDivisionRecord::getBatchOrderId, batchOrderId)
                                .orderByAsc(PayOrderDivisionRecord::getRecordId)
                        );

                        if(recordList == null || recordList.isEmpty()){
                            continue;
                        }

                        // 查询支付订单信息
                        PayOrder payOrder = payOrderService.getById(batchRecord.getPayOrderId());
                        if (payOrder == null) {
                            log.error("支付订单记录不存在：{}",  batchRecord.getPayOrderId());
                            continue;
                        }
                        // 查询转账接口是否存在
                        IDivisionService divisionService = SpringBeansUtil.getBean(payOrder.getIfCode() + "DivisionService", IDivisionService.class);

                        if (divisionService == null) {
                            log.error("查询分账接口不存在：{}",  payOrder.getIfCode());
                            continue;
                        }
                        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId());
                        // 调用渠道侧的查单接口：   注意：  渠道内需保证：
                        // 1. 返回的条目 必须全部来自recordList， 可以少于recordList但是不得高于 recordList 数量；
                        // 2. recordList 的记录可能与接口返回的数量不一致，  接口实现不要求对条目数量做验证；
                        // 3. 接口查询的记录若recordList 不存在， 忽略即可。  （  例如两条相同的accNo, 则可能仅匹配一条。 那么另外一条将在下一次循环中处理。  ）
                        // 4. 仅明确状态的再返回，若不明确则不需返回；
                        HashMap<Long, ChannelRetMsg> queryDivision = divisionService.queryDivision(payOrder, recordList, mchAppConfigContext);

                        // 处理查询结果
                        recordList.stream().forEach(record -> {
                            ChannelRetMsg channelRetMsg = queryDivision.get(record.getRecordId());

                            // 响应状态为分账成功或失败时，更新该记录状态
                            if (ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == channelRetMsg.getChannelState() ||
                                    ChannelRetMsg.ChannelState.CONFIRM_FAIL == channelRetMsg.getChannelState()) {

                                Byte state = ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == channelRetMsg.getChannelState() ? PayOrderDivisionRecord.STATE_SUCCESS : PayOrderDivisionRecord.STATE_FAIL;
                                // 更新记录状态
                                payOrderDivisionRecordService.updateRecordSuccessOrFailBySingleItem(record.getRecordId(), state, channelRetMsg.getChannelErrMsg());
                            }
                        });

                    } catch (Exception e1) {
                        log.error("处理补单任务单条[{}]异常",  batchRecord.getBatchOrderId(), e1);
                    }
                }

                //已经到达页码最大量，无需再次查询
                if(pageRecordList.getPages() <= currentPageIndex){
                    break;
                }
                currentPageIndex++;


            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.error("处理分账补单任务, error", e);
                break;
            }

        }
    }

}
