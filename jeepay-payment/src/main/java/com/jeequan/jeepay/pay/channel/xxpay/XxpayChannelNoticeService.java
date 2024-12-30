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
package com.jeequan.jeepay.pay.channel.xxpay;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.xxpay.XxpayNormalMchParams;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

/*
* 小新支付 支付回调接口实现类
*
* @author jmdhappy
* @site https://www.jeequan.com
* @date 2021/9/21 00:16
*/
@Service
@Slf4j
public class XxpayChannelNoticeService extends AbstractChannelNoticeService {


    @Override
    public String getIfCode() {
        return CS.IF_CODE.XXPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {

            JSONObject params = getReqParamJSON();
            String payOrderId = params.getString("mchOrderNo");
            return MutablePair.of(payOrderId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }



    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {
            XxpayNormalMchParams xxpayParams = (XxpayNormalMchParams)configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;
            String checkSign = jsonParams.getString("sign");
            jsonParams.remove("sign");
            // 验证签名
            if(!checkSign.equals(XxpayKit.getSign(jsonParams, xxpayParams.getKey()))) {
                throw ResponseException.buildText("ERROR");
            }

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("success");

            // 支付状态,0-订单生成,1-支付中,2-支付成功,3-业务处理完成
            String status = jsonParams.getString("status");

            ChannelRetMsg result = new ChannelRetMsg();
            result.setChannelOrderId(jsonParams.getString("channelOrderNo")); //渠道订单号
            result.setResponseEntity(okResponse); //响应数据

            result.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中

            if("2".equals(status) || "3".equals(status)){
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            }

            return result;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

}
