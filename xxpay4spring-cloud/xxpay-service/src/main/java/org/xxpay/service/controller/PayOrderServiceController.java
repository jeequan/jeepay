package org.xxpay.service.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.common.util.MyBase64;
import org.xxpay.common.util.MyLog;
import org.xxpay.dal.dao.model.PayOrder;
import org.xxpay.service.channel.tencent.service.UnifiedOrderService;
import org.xxpay.service.service.PayOrderService;

/**
 * @Description: 支付订单接口
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@RestController
public class PayOrderServiceController {

    private final MyLog _log = MyLog.getLog(PayOrderServiceController.class);

    @Autowired
    private DiscoveryClient client;

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    UnifiedOrderService unifiedOrderService;

    @RequestMapping(value = "/pay/create")
    public String createPayOrder(@RequestParam String jsonParam) {
        // TODO 参数校验
        PayOrder payOrder = JSON.parseObject(new String(MyBase64.decode(jsonParam)), PayOrder.class);
        int result = payOrderService.createPayOrder(payOrder);
        JSONObject retObj = new JSONObject();
        retObj.put("result", result);
        return retObj.toJSONString();
    }

}
