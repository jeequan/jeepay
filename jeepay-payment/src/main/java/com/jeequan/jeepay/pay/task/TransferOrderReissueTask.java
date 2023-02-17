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
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.pay.service.TransferOrderReissueService;
import com.jeequan.jeepay.service.impl.TransferOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/*
* 转账补单定时任务
*
* @author zx
* @site https://www.jeequan.com
* @date 2022/12/29 17:47
*/
@Slf4j
@Component
public class TransferOrderReissueTask {

    private static final int QUERY_PAGE_SIZE = 100; //每次查询数量

    @Autowired private TransferOrderService transferOrderService;
    @Autowired private TransferOrderReissueService transferOrderReissueService;

    @Scheduled(cron="0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        //查询条件：
        LambdaQueryWrapper<TransferOrder> lambdaQueryWrapper = TransferOrder.gw()
                .eq(TransferOrder::getState, TransferOrder.STATE_ING) // 转账中
                .ge(TransferOrder::getCreatedAt, DateUtil.offsetDay(new Date(), -1)); // 只查询一天内的转账单;

        int currentPageIndex = 1; //当前页码
        while(true){

            try {
                IPage<TransferOrder> iPage = transferOrderService.page(new Page(currentPageIndex, QUERY_PAGE_SIZE), lambdaQueryWrapper);

                if(iPage == null || iPage.getRecords().isEmpty()){ //本次查询无结果, 不再继续查询;
                    break;
                }

                for(TransferOrder transferOrder: iPage.getRecords()){
                    transferOrderReissueService.processOrder(transferOrder);
                }

                //已经到达页码最大量，无需再次查询
                if(iPage.getPages() <= currentPageIndex){
                    break;
                }
                currentPageIndex++;

            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.error("error", e);
                break;
            }
        }
    }



}
