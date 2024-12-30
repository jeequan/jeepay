package com.jeequan.jeepay.pay.channel.pppay;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.ResponseException;
import com.jeequan.jeepay.pay.channel.AbstractChannelNoticeService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * none.
 *
 * @author 陈泉
 * @package com.jeequan.jeepay.pay.channel.pppay
 * @create 2021/11/15 20:58
 */
@Service
@Slf4j
public class PppayChannelNoticeService extends AbstractChannelNoticeService {
    @Override
    public String getIfCode() {
        return CS.IF_CODE.PPPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId,
                                                   NoticeTypeEnum noticeTypeEnum) {
        // 同步和异步需要不同的解析方案
        // 异步需要从 webhook 中读取，所以这里读取方式不太一样
        if (noticeTypeEnum == NoticeTypeEnum.DO_NOTIFY) {
            JSONObject params = JSONUtil.parseObj(getReqParamJSON().toJSONString());
            String orderId = params.getByPath("resource.purchase_units[0].invoice_id", String.class);
            return MutablePair.of(orderId, params);
        } else {
            if (urlOrderId == null || urlOrderId.isEmpty()) {
                throw ResponseException.buildText("ERROR");
            }
            try {
                JSONObject params = JSONUtil.parseObj(getReqParamJSON().toString());
                return MutablePair.of(urlOrderId, params);
            } catch (Exception e) {
                log.error("error", e);
                throw ResponseException.buildText("ERROR");
            }
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayOrder payOrder,
                                  MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {
            if (noticeTypeEnum == NoticeTypeEnum.DO_RETURN) {
                return doReturn(request, params, payOrder, mchAppConfigContext);
            }
            return doNotify(request, params, payOrder, mchAppConfigContext);
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    public ChannelRetMsg doReturn(HttpServletRequest request, Object params, PayOrder payOrder,
                                  MchAppConfigContext mchAppConfigContext) throws IOException {
        JSONObject object = (JSONObject) params;
        // 获取 Paypal 订单 ID
        String ppOrderId = object.getStr("token");
        // 统一处理订单
        return mchAppConfigContext.getPaypalWrapper().processOrder(ppOrderId, payOrder);
    }

    public ChannelRetMsg doNotify(HttpServletRequest request, Object params, PayOrder payOrder,
                                  MchAppConfigContext mchAppConfigContext) throws IOException {
        JSONObject object = (JSONObject) params;
        // 获取 Paypal 订单 ID
        String ppOrderId = object.getByPath("resource.id", String.class);
        // 统一处理订单
        return mchAppConfigContext.getPaypalWrapper().processOrder(ppOrderId, payOrder, true);
    }
}
