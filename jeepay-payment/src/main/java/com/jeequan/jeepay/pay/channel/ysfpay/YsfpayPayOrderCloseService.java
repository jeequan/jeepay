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
package com.jeequan.jeepay.pay.channel.ysfpay;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.channel.IPayOrderCloseService;
import com.jeequan.jeepay.pay.channel.IPayOrderQueryService;
import com.jeequan.jeepay.pay.channel.ysfpay.utils.YsfHttpUtil;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 云闪付 关闭订单接口实现类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@Service
@Slf4j
public class YsfpayPayOrderCloseService implements IPayOrderCloseService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
    }

    @Autowired
    private YsfpayPaymentService ysfpayPaymentService;

    @Override
    public ChannelRetMsg close(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        JSONObject reqParams = new JSONObject();
        String orderType = YsfHttpUtil.getOrderTypeByCommon(payOrder.getWayCode());
        String logPrefix = "【云闪付("+orderType+")关闭订单】";

        try {
            reqParams.put("orderNo", payOrder.getPayOrderId()); //订单号
            reqParams.put("orderType", orderType); //订单类型

            //封装公共参数 & 签名 & 调起http请求 & 返回响应数据并包装为json格式。
            JSONObject resJSON = ysfpayPaymentService.packageParamAndReq("/gateway/api/pay/closeOrder", reqParams, logPrefix, mchAppConfigContext);
            log.info("关闭订单 payorderId:{}, 返回结果:{}", payOrder.getPayOrderId(), resJSON);
            if(resJSON == null){
                return ChannelRetMsg.sysError("【云闪付】请求关闭订单异常");
            }

            //请求 & 响应成功， 判断业务逻辑
            String respCode = resJSON.getString("respCode"); //应答码
            String respMsg = resJSON.getString("respMsg"); //应答信息
            if(("00").equals(respCode)){// 请求成功
                return ChannelRetMsg.confirmSuccess(null);  //关单成功
            }
            return ChannelRetMsg.sysError(respMsg); // 关单失败
        }catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage()); // 关单失败
        }
    }

}
