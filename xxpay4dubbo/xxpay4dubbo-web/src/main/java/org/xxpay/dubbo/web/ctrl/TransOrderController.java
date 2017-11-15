package org.xxpay.dubbo.web.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.common.constant.PayConstant;
import org.xxpay.common.util.MyLog;
import org.xxpay.common.util.MySeq;
import org.xxpay.common.util.XXPayUtil;
import org.xxpay.dubbo.web.service.MchInfoService;
import org.xxpay.dubbo.web.service.PayChannelService;
import org.xxpay.dubbo.web.service.TransOrderService;

import java.util.Map;

/**
 * @Description: 转账订单
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-10-30
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@RestController
public class TransOrderController {

    private final MyLog _log = MyLog.getLog(TransOrderController.class);

    @Autowired
    private TransOrderService transOrderService;

    @Autowired
    private PayChannelService payChannelService;

    @Autowired
    private MchInfoService mchInfoService;

    /**
     * 统一转账接口:
     * 1)先验证接口参数以及签名信息
     * 2)验证通过创建支付订单
     * 3)根据商户选择渠道,调用支付服务进行下单
     * 4)返回下单数据
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/trans/create_order")
    public String payOrder(@RequestParam String params) {
        _log.info("###### 开始接收商户统一转账请求 ######");
        String logPrefix = "【商户统一转账】";
        try {
            JSONObject po = JSONObject.parseObject(params);
            JSONObject transContext = new JSONObject();
            JSONObject transOrder = null;
            // 验证参数有效性
            Object object = validateParams(po, transContext);
            if (object instanceof String) {
                _log.info("{}参数校验不通过:{}", logPrefix, object);
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, object.toString(), null, null));
            }
            if (object instanceof JSONObject) transOrder = (JSONObject) object;
            if(transOrder == null) return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心转账失败", null, null));
            int result = transOrderService.create(transOrder);
            _log.info("{}创建转账订单,结果:{}", logPrefix, result);
            if(result != 1) {
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "创建转账订单失败", null, null));
            }
            // 发送异步转账消息
            String transOrderId = transOrder.getString("transOrderId");
            String channelName = transContext.getString("channelName");
            transOrderService.sendTransNotify(transOrderId, channelName);
            _log.info("{}发送转账任务完成,transOrderId={}", logPrefix, transOrderId);
            Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
            map.put("transOrderId", transOrderId);
            return XXPayUtil.makeRetData(map, transContext.getString("resKey"));
        }catch (Exception e) {
            _log.error(e, "");
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心系统异常", null, null));
        }
    }

    /**
     * 验证创建订单请求参数,参数通过返回JSONObject对象,否则返回错误文本信息
     * @param params
     * @return
     */
    private Object validateParams(JSONObject params, JSONObject transContext) {
        // 验证请求参数,参数有问题返回错误提示
        String errorMessage;
        // 支付参数
        String mchId = params.getString("mchId"); 			    // 商户ID
        String mchTransNo = params.getString("mchTransNo"); 	// 商户转账单号
        String channelId = params.getString("channelId"); 	    // 渠道ID
        String amount = params.getString("amount"); 		    // 转账金额（单位分）
        String currency = params.getString("currency");         // 币种
        String clientIp = params.getString("clientIp");	        // 客户端IP
        String device = params.getString("device"); 	        // 设备
        String extra = params.getString("extra");		        // 特定渠道发起时额外参数
        String param1 = params.getString("param1"); 		    // 扩展参数1
        String param2 = params.getString("param2"); 		    // 扩展参数2
        String notifyUrl = params.getString("notifyUrl"); 		// 转账结果回调URL
        String sign = params.getString("sign"); 				// 签名
        String channelUser = params.getString("channelUser");	// 渠道用户标识,如微信openId,支付宝账号
        String userName = params.getString("userName");	        // 用户姓名
        String remarkInfo = params.getString("remarkInfo");	    // 备注
        // 验证请求参数有效性（必选项）
        if(StringUtils.isBlank(mchId)) {
            errorMessage = "request params[mchId] error.";
            return errorMessage;
        }
        if(StringUtils.isBlank(mchTransNo)) {
            errorMessage = "request params[mchTransNo] error.";
            return errorMessage;
        }
        if(StringUtils.isBlank(channelId)) {
            errorMessage = "request params[channelId] error.";
            return errorMessage;
        }
        if(!NumberUtils.isNumber(amount)) {
            errorMessage = "request params[amount] error.";
            return errorMessage;
        }
        if(StringUtils.isBlank(currency)) {
            errorMessage = "request params[currency] error.";
            return errorMessage;
        }
        if(StringUtils.isBlank(notifyUrl)) {
            errorMessage = "request params[notifyUrl] error.";
            return errorMessage;
        }
        if(StringUtils.isBlank(channelUser)) {
            errorMessage = "request params[channelUser] error.";
            return errorMessage;
        }
        if(StringUtils.isBlank(remarkInfo)) {
            errorMessage = "request params[remarkInfo] error.";
            return errorMessage;
        }

        // 签名信息
        if (StringUtils.isEmpty(sign)) {
            errorMessage = "request params[sign] error.";
            return errorMessage;
        }

        // 查询商户信息
        JSONObject mchInfo = mchInfoService.getByMchId(mchId);
        if(mchInfo == null) {
            errorMessage = "Can't found mchInfo[mchId="+mchId+"] record in db.";
            return errorMessage;
        }
        if(mchInfo.getByte("state") != 1) {
            errorMessage = "mchInfo not available [mchId="+mchId+"] record in db.";
            return errorMessage;
        }

        String reqKey = mchInfo.getString("reqKey");
        if (StringUtils.isBlank(reqKey)) {
            errorMessage = "reqKey is null[mchId="+mchId+"] record in db.";
            return errorMessage;
        }
        transContext.put("resKey", mchInfo.getString("resKey"));

        // 查询商户对应的支付渠道
        JSONObject payChannel = payChannelService.getByMchIdAndChannelId(mchId, channelId);
        if(payChannel == null) {
            errorMessage = "Can't found payChannel[channelId="+channelId+",mchId="+mchId+"] record in db.";
            return errorMessage;
        }
        if(payChannel.getByte("state") != 1) {
            errorMessage = "channel not available [channelId="+channelId+",mchId="+mchId+"]";
            return errorMessage;
        }
        transContext.put("channelName", payChannel.getString("channelName"));

        // 验证签名数据
        boolean verifyFlag = XXPayUtil.verifyPaySign(params, reqKey);
        if(!verifyFlag) {
            errorMessage = "Verify XX trans sign failed.";
            return errorMessage;
        }
        // 验证参数通过,返回JSONObject对象
        JSONObject transOrder = new JSONObject();
        transOrder.put("transOrderId", MySeq.getTrans());
        transOrder.put("mchId", mchId);
        transOrder.put("mchTransNo", mchTransNo);
        transOrder.put("channelId", channelId);
        transOrder.put("amount", Long.parseLong(amount));
        transOrder.put("currency", currency);
        transOrder.put("clientIp", clientIp);
        transOrder.put("device", device);
        transOrder.put("channelUser", channelUser);
        transOrder.put("userName", userName);
        transOrder.put("remarkInfo", remarkInfo);
        transOrder.put("extra", extra);
        transOrder.put("channelMchId", payChannel.getString("channelMchId"));
        transOrder.put("param1", param1);
        transOrder.put("param2", param2);
        transOrder.put("notifyUrl", notifyUrl);
        return transOrder;
    }

}
