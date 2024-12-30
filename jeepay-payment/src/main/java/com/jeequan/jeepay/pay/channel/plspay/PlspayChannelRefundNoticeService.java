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
package com.jeequan.jeepay.pay.channel.plspay;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.core.model.params.plspay.PlspayConfig;
import com.jeequan.jeepay.core.model.params.plspay.PlspayNormalMchParams;
import com.jeequan.jeepay.pay.channel.AbstractChannelRefundNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.util.JeepayKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

/*
 * 计全支付plus 退款回调接口实现类
 *
 * @author yr
 * @site https://www.jeequan.com
 * @date 2022/7/20 10:31
 */
@Service
@Slf4j
public class PlspayChannelRefundNoticeService extends AbstractChannelRefundNoticeService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.PLSPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {
            JSONObject params = getReqParamJSON();
            String refundOrderId = params.getString("mchRefundNo");
            return MutablePair.of(refundOrderId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, RefundOrder refundOrder, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {
            String logPrefix = "【处理计全付退款回调】";
            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;
            log.info("{} 回调参数, jsonParams：{}", logPrefix, jsonParams);
            // 校验签名
            String sign = jsonParams.getString("sign");
            boolean verifyResult = verifyParams(jsonParams, sign, mchAppConfigContext);
            // 验证参数失败
            if (!verifyResult) {
                throw ResponseException.buildText("ERROR");
            }
            log.info("{}验证退款通知数据及签名通过", logPrefix);
            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("success");

            ChannelRetMsg result = new ChannelRetMsg();
            result.setChannelOrderId(jsonParams.getString("refundOrderId")); //渠道订单号
            result.setResponseEntity(okResponse); //响应数据
            result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);

            return result;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    /**
     * 验证计全支付plus通知参数
     *
     * @return boolean  true or false
     */
    public boolean verifyParams(JSONObject jsonParams, String sign, MchAppConfigContext mchAppConfigContext) {
        try {
            // 返回数据
            if (StringUtils.isEmpty(sign)) {
                log.info("验签参数为空 [sign] :{}", sign);
                return false;
            }
            PlspayNormalMchParams normalMchParams = (PlspayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.PLSPAY);
            jsonParams.remove("sign");
            // 获取md5秘钥,生成签名
            String newSign = JeepayKit.getSign(jsonParams, normalMchParams.getAppSecret());
            // 验签  异步时都是MD5
            if (!sign.equals(newSign)) {
                log.info("验签失败！ 回调参数：parameter = {}", jsonParams);
                return false;
            }
            // 退款状态 0-订单生成 1-退款中 2-退款成功 3-退款失败 4-退款关闭
            String status = jsonParams.getString("state");
            if (!PlspayConfig.REFUND_STATE_SUCCESS.equals(status)) {
                log.info("订单状态错误！ state = {}", status);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

}
