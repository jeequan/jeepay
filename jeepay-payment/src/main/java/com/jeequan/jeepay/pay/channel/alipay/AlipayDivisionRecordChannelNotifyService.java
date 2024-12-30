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
package com.jeequan.jeepay.pay.channel.alipay;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.alipay.AlipayConfig;
import com.jeequan.jeepay.core.model.params.alipay.AlipayIsvParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.pay.channel.AbstractDivisionRecordChannelNotifyService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.msg.DivisionChannelNotifyModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/*
* 支付宝 分账回调接口实现类
*
* 注意：
*
// royalty_mode 必须传入：  async ( 使用异步：  需要 1、请配置 支付宝应用的网关地址 （ xxx.com/api/channelbiz/alipay/appGatewayMsgReceive ）， 2、 订阅消息。   )
// 2023-03-30 咨询支付宝客服：  如果没有传royalty_mode分账模式,这个默认会是同步分账,同步分账不需要关注异步通知,接口调用成功就分账成功了  2,同步分账默认不会给您发送异步通知。
// 3. 服务商代商户调用商家分账，当异步分账时服务商必须调用alipay.open.app.message.topic.subscribe(订阅消息主题)对消息api做关联绑定，服务商才会收到alipay.trade.order.settle.notify通知，否则服务商无法收到通知。
// https://opendocs.alipay.com/open/20190308105425129272/quickstart#%E8%9A%82%E8%9A%81%E6%B6%88%E6%81%AF%EF%BC%9A%E4%BA%A4%E6%98%93%E5%88%86%E8%B4%A6%E7%BB%93%E6%9E%9C%E9%80%9A%E7%9F%A5
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2023/3/30 12:20
*/
@Service
@Slf4j
public class AlipayDivisionRecordChannelNotifyService extends AbstractDivisionRecordChannelNotifyService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request) {

        try {
            JSONObject params = getReqParamJSON();

            String batchOrderId = params.getJSONObject("biz_content").getString("out_request_no"); // 分账批次号
            return MutablePair.of(batchOrderId, params);

        } catch (Exception e) {

            log.error("error", e);
            throw ResponseException.buildText("ERROR");

        }
    }

    @Override
    public DivisionChannelNotifyModel doNotify(HttpServletRequest request, Object params, List<PayOrderDivisionRecord> recordList, MchAppConfigContext mchAppConfigContext) {

        // 响应结果
        DivisionChannelNotifyModel result = new DivisionChannelNotifyModel();

        try {

            //配置参数获取
            Byte useCert = null;
            String alipaySignType, alipayPublicCert, alipayPublicKey = null;
            if(mchAppConfigContext.isIsvsubMch()){

                // 获取支付参数
                AlipayIsvParams alipayParams = (AlipayIsvParams)configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), getIfCode());
                useCert = alipayParams.getUseCert();
                alipaySignType = alipayParams.getSignType();
                alipayPublicCert = alipayParams.getAlipayPublicCert();
                alipayPublicKey = alipayParams.getAlipayPublicKey();

            }else{

                // 获取支付参数
                AlipayNormalMchParams alipayParams = (AlipayNormalMchParams)configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

                useCert = alipayParams.getUseCert();
                alipaySignType = alipayParams.getSignType();
                alipayPublicCert = alipayParams.getAlipayPublicCert();
                alipayPublicKey = alipayParams.getAlipayPublicKey();
            }

            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;

            boolean verifyResult;
            if(useCert != null && useCert == CS.YES){  //证书方式

                verifyResult = AlipaySignature.rsaCertCheckV1(jsonParams.toJavaObject(Map.class), getCertFilePath(alipayPublicCert),
                        AlipayConfig.CHARSET, alipaySignType);

            }else{
                verifyResult = AlipaySignature.rsaCheckV1(jsonParams.toJavaObject(Map.class), alipayPublicKey, AlipayConfig.CHARSET, alipaySignType);
            }

            //验签失败
            if(!verifyResult){
                throw ResponseException.buildText("ERROR");
            }

            // 得到所有的 accNo 与 recordId map
            Map<String, Long> accnoAndRecordIdSet = new HashMap<>();
            for (PayOrderDivisionRecord record : recordList) {
                accnoAndRecordIdSet.put(record.getAccNo(), record.getRecordId());
            }

            Map<Long, ChannelRetMsg> recordResultMap = new HashMap<>();

            JSONObject bizContentJSON = jsonParams.getJSONObject("biz_content");

            // 循环
            JSONArray array = bizContentJSON.getJSONArray("royalty_detail_list");
            for (Object o : array) {
                JSONObject itemJSON = (JSONObject) o;

                // 我方系统的分账接收记录ID
                Long recordId = accnoAndRecordIdSet.get(itemJSON.getString("trans_in"));

                // 分账类型 && 包含该笔分账账号
                if("transfer".equals(itemJSON.getString("operation_type")) && recordId != null){

                    // 分账成功
                    if("SUCCESS".equals(itemJSON.getString("state"))){
                        recordResultMap.put(recordId, ChannelRetMsg.confirmSuccess(bizContentJSON.getString("settle_no")));
                    }

                    // 分账失败
                    if("FAIL".equals(itemJSON.getString("state"))){
                        recordResultMap.put(recordId, ChannelRetMsg.confirmFail(bizContentJSON.getString("settle_no"), itemJSON.getString("error_code"), itemJSON.getString("error_desc")));
                    }
                }
            }

            result.setRecordResultMap(recordResultMap);
            result.setApiRes(textResp("success"));

            return result;

        } catch (Exception e) {

            log.error("error", e);
            throw ResponseException.buildText("ERROR");

        }
    }


}
