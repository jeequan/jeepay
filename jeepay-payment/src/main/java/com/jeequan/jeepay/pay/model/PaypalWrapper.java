package com.jeequan.jeepay.pay.model;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.pppay.PpPayNormalMchParams;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayNormalMchParams;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.serializer.Json;
import com.paypal.orders.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * none.
 *
 * @author 陈泉
 * @package com.jeequan.jeepay.pay.model
 * @create 2021/11/15 19:10
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaypalWrapper {
    private PayPalEnvironment environment;
    private PayPalHttpClient client;

    private String notifyWebhook;
    private String refundWebhook;

    public ChannelRetMsg processOrder(String token, PayOrder payOrder) throws IOException {
        return processOrder(token, payOrder, false);
    }

    public List<String> processOrder(String order) {
        return processOrder(order, "null");
    }

    // 解析拼接 ID
    public List<String> processOrder(String order, String afterOrderId) {
        String ppOrderId = "null";
        String ppCatptId = "null";
        if (order != null) {
            if (order.contains(",")) {
                String[] split = order.split(",");
                if (split.length == 2) {
                    ppCatptId = split[1];
                    ppOrderId = split[0];
                }
            }
        }
        if (afterOrderId != null && !"null".equalsIgnoreCase(afterOrderId)) {
            ppOrderId = afterOrderId;
        }

        if ("null".equalsIgnoreCase(ppCatptId)) {
            ppCatptId = null;
        }
        if ("null".equalsIgnoreCase(ppOrderId)) {
            ppOrderId = null;
        }

        return Arrays.asList(ppOrderId, ppCatptId);
    }

    /**
     * 处理并捕获订单
     * 由于 Paypal 创建订单后需要进行一次 Capture(捕获) 才可以正确获取到订单的支付状态
     *
     * @param token
     * @param payOrder
     * @param isCapture
     * @return
     * @throws IOException
     */
    public ChannelRetMsg processOrder(String token, PayOrder payOrder, boolean isCapture) throws IOException {
        // Paypal 创建订单存在一个 Token，当订单捕获之后会有一个 捕获的ID ，退款需要用到
        String ppOrderId = this.processOrder(payOrder.getChannelOrderNo(), token).get(0);
        String ppCatptId = this.processOrder(payOrder.getChannelOrderNo()).get(1);

        ChannelRetMsg channelRetMsg = ChannelRetMsg.waiting();
        channelRetMsg.setResponseEntity(textResp("ERROR"));

        // 如果订单 ID 还不存在，等待
        if (ppOrderId == null) {
            channelRetMsg.setChannelErrCode("201");
            channelRetMsg.setChannelErrMsg("捕获订单请求失败");
            return channelRetMsg;
        } else {
            Order order;

            channelRetMsg.setChannelOrderId(ppOrderId + "," + "null");

            // 如果 捕获 ID 不存在
            if (ppCatptId == null && isCapture) {
                OrderRequest orderRequest = new OrderRequest();
                OrdersCaptureRequest ordersCaptureRequest = new OrdersCaptureRequest(ppOrderId);
                ordersCaptureRequest.requestBody(orderRequest);

                // 捕获订单
                HttpResponse<Order> response = this.getClient().execute(ordersCaptureRequest);

                if (response.statusCode() != 201) {
                    channelRetMsg.setChannelErrCode("201");
                    channelRetMsg.setChannelErrMsg("捕获订单请求失败");
                    return channelRetMsg;
                }
                order = response.result();
            } else {
                OrdersGetRequest request = new OrdersGetRequest(ppOrderId);
                HttpResponse<Order> response = this.getClient().execute(request);

                if (response.statusCode() != 200) {
                    channelRetMsg.setChannelOrderId(ppOrderId);
                    channelRetMsg.setChannelErrCode("200");
                    channelRetMsg.setChannelErrMsg("请求订单详情失败");
                    return channelRetMsg;
                }

                order = response.result();
            }

            String status = order.status();
            String orderJsonStr = new Json().serialize(order);
            JSONObject orderJson = JSONUtil.parseObj(orderJsonStr);

            for (PurchaseUnit purchaseUnit : order.purchaseUnits()) {
                if (purchaseUnit.payments() != null) {
                    for (Capture capture : purchaseUnit.payments().captures()) {
                        ppCatptId = capture.id();
                        break;
                    }
                }
            }

            String orderUserId = orderJson.getByPath("payer.payer_id", String.class);

            ChannelRetMsg result = new ChannelRetMsg();
            result.setNeedQuery(true);
            result.setChannelOrderId(ppOrderId + "," + ppCatptId); // 渠道订单号
            result.setChannelUserId(orderUserId);  // 支付用户ID
            result.setChannelAttach(orderJsonStr); // Capture 响应数据
            result.setResponseEntity(textResp("SUCCESS")); // 响应数据
            result.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中
            result = dispatchCode(status, result); // 处理状态码
            return result;
        }
    }

    /**
     * 处理 Paypal 状态码
     *
     * @param status        状态码
     * @param channelRetMsg 通知信息
     * @return 通知信息
     */
    public ChannelRetMsg dispatchCode(String status, ChannelRetMsg channelRetMsg) {
        if ("SAVED".equalsIgnoreCase(status)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        } else if ("APPROVED".equalsIgnoreCase(status)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        } else if ("VOIDED".equalsIgnoreCase(status)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
        } else if ("PAYER_ACTION_REQUIRED".equalsIgnoreCase(status)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        } else if ("CREATED".equalsIgnoreCase(status)) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        } else {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.UNKNOWN);
        }
        return channelRetMsg;
    }

    public ResponseEntity textResp(String text) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity(text, httpHeaders, HttpStatus.OK);
    }

    public static PaypalWrapper buildPaypalWrapper(PpPayNormalMchParams ppPayNormalMchParams){
        PaypalWrapper paypalWrapper = new PaypalWrapper();
        PayPalEnvironment environment = new PayPalEnvironment.Live(ppPayNormalMchParams.getClientId(), ppPayNormalMchParams.getSecret());
        if (ppPayNormalMchParams.getSandbox() == 1) {
            environment = new PayPalEnvironment.Sandbox(ppPayNormalMchParams.getClientId(), ppPayNormalMchParams.getSecret());
        }
        paypalWrapper.setEnvironment(environment);
        paypalWrapper.setClient(new PayPalHttpClient(environment));
        paypalWrapper.setNotifyWebhook(ppPayNormalMchParams.getNotifyWebhook());
        paypalWrapper.setRefundWebhook(ppPayNormalMchParams.getRefundWebhook());
        return paypalWrapper;

    }

}
