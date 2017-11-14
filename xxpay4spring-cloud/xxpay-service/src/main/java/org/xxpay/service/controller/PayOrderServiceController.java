package org.xxpay.service.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.MyBase64;
import org.xxpay.common.util.MyLog;
import org.xxpay.dal.dao.model.PayOrder;
import org.xxpay.service.service.PayOrderService;

/**
 * @Description: 支付订单接口
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@RestController
public class PayOrderServiceController extends Notify4BasePay {

    private final MyLog _log = MyLog.getLog(PayOrderServiceController.class);

    @Autowired
    private PayOrderService payOrderService;

    @RequestMapping(value = "/pay/create")
    public String createPayOrder(@RequestParam String jsonParam) {
        _log.info("接收创建支付订单请求,jsonParam={}", jsonParam);
        JSONObject retObj = new JSONObject();
        retObj.put("code", "0000");
        if(StringUtils.isBlank(jsonParam)) {
            retObj.put("code", "0001");
            retObj.put("msg", "缺少参数");
            return retObj.toJSONString();
        }
        try {
            PayOrder payOrder = JSON.parseObject(new String(MyBase64.decode(jsonParam)), PayOrder.class);
            int result = payOrderService.createPayOrder(payOrder);
            retObj.put("result", result);
        }catch (Exception e) {
            retObj.put("code", "9999"); // 系统错误
            retObj.put("msg", "系统错误");
        }
        return retObj.toJSONString();
    }

    @RequestMapping(value = "/pay/query")
    public String queryPayOrder(@RequestParam String jsonParam) {
        _log.info("selectPayOrder << {}", jsonParam);
        JSONObject retObj = new JSONObject();
        retObj.put("code", "0000");
        if(StringUtils.isBlank(jsonParam)) {
            retObj.put("code", "0001"); // 参数错误
            retObj.put("msg", "缺少参数");
            return retObj.toJSONString();
        }
        JSONObject paramObj = JSON.parseObject(new String(MyBase64.decode(jsonParam)));
        String mchId = paramObj.getString("mchId");
        String payOrderId = paramObj.getString("payOrderId");
        String mchOrderNo = paramObj.getString("mchOrderNo");
        PayOrder payOrder;
        if(StringUtils.isNotBlank(payOrderId)) {
            payOrder = payOrderService.selectPayOrderByMchIdAndPayOrderId(mchId, payOrderId);
        }else {
            payOrder = payOrderService.selectPayOrderByMchIdAndMchOrderNo(mchId, mchOrderNo);
        }
        if(payOrder == null) {
            retObj.put("code", "0002");
            retObj.put("msg", "支付订单不存在");
            return retObj.toJSONString();
        }

        //
        boolean executeNotify = paramObj.getBooleanValue("executeNotify");
        // 如果选择回调且支付状态为支付成功,则回调业务系统
        if(executeNotify && payOrder.getStatus() == PayConstant.PAY_STATUS_SUCCESS) {
            this.doNotify(payOrder);
        }
        retObj.put("result", JSON.toJSON(payOrder));
        _log.info("selectPayOrder >> {}", retObj);
        return retObj.toJSONString();
    }

}
