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

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.alipay.AlipayConfig;
import com.jeequan.jeepay.core.model.params.alipay.AlipayIsvParams;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.pay.channel.AbstractTransferNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/*
* 支付宝 转账回调接口实现类
*
* @author zx
* @site https://www.jeequan.com
* @date 2021/21/01 17:16
*/
@Service
@Slf4j
public class AlipayTransferNoticeService extends AbstractTransferNoticeService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId) {

        try {

            JSONObject params = getReqParamJSON();
            log.info("【支付宝转账】回调通知参数：{}", params.toJSONString());

            JSONObject bizContent = JSONObject.parseObject(params.getString("biz_content"));

            String transferId = bizContent.getString("out_biz_no");
            return MutablePair.of(transferId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }


    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext) {

        String logPrefix = "【支付宝转账通知】";

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
            JSONObject bizContent = JSONObject.parseObject(jsonParams.getString("biz_content"));

            boolean verifyResult;
            if(useCert != null && useCert == CS.YES){  //证书方式

                verifyResult = AlipaySignature.rsaCertCheckV1(jsonParams.toJavaObject(Map.class), getCertFilePath(alipayPublicCert),
                        AlipayConfig.CHARSET, alipaySignType);

            }else{
                verifyResult = AlipaySignature.rsaCheckV1(jsonParams.toJavaObject(Map.class), alipayPublicKey, AlipayConfig.CHARSET, alipaySignType);
            }

            //验签失败
            if(!verifyResult){
                log.error("{}，验签失败", logPrefix);
                throw ResponseException.buildText("ERROR");
            }

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("SUCCESS");

            ChannelRetMsg channelRetMsg = new ChannelRetMsg();
            channelRetMsg.setResponseEntity(okResponse); // 响应数据

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认转账中

            // 成功－SUCCESS
            String status = bizContent.getString("status");
            if("SUCCESS".equals(status)){
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            }

            return channelRetMsg;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

}
