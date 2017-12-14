package org.xxpay.dubbo.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xxpay.common.util.MySeq;
import org.xxpay.common.util.RpcUtil;
import org.xxpay.dal.dao.model.RefundOrder;
import org.xxpay.dal.dao.model.TransOrder;
import org.xxpay.dubbo.api.service.IPayChannel4AliService;
import org.xxpay.dubbo.api.service.IPayChannel4WxService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/10/27
 * @description:
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PayServiceTest {

    @Autowired
    IPayChannel4AliService payChannel4AliService;

    @Autowired
    IPayChannel4WxService payChannel4WxService;

    String TransOrderId = System.currentTimeMillis()+"";

    @Test
    public void testDoAliTransReq() {
        TransOrder transOrder = new TransOrder();
        transOrder.setTransOrderId(TransOrderId);
        transOrder.setMchId("20001223");
        transOrder.setChannelId("ALIPAY_PC");
        transOrder.setChannelUser("jmdhappy@126.com");
        transOrder.setAmount(10l);
        transOrder.setRemarkInfo("测试XxPay转账");
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("transOrder", transOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map map = payChannel4AliService.doAliTransReq(jsonParam);
        System.out.println("map=" + map);
    }

    @Test
    public void testGetAliTransReq() {
        TransOrder transOrder = new TransOrder();
        //transOrder.setTransOrderId("1509098344835");
        transOrder.setChannelOrderNo("302892158947140");
        transOrder.setMchId("20001223");
        transOrder.setChannelId("ALIPAY_PC");

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("transOrder", transOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map map = payChannel4AliService.getAliTransReq(jsonParam);
        System.out.println("map=" + map);
    }

    @Test
    public void testDoAliRefundReq() {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(MySeq.getRefund());
        refundOrder.setPayOrderId("P0020171028110830000001");
        refundOrder.setChannelPayOrderNo("2017102821001003030281781741");
        refundOrder.setRefundAmount(100l);

        refundOrder.setMchId("20001223");
        refundOrder.setChannelId("ALIPAY_PC");
        refundOrder.setChannelUser("jmdhappy@126.com");


        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("refundOrder", refundOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map map = payChannel4AliService.doAliRefundReq(jsonParam);
        System.out.println("map=" + map);
    }

    @Test
    public void testetAliRefundReq() {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(MySeq.getRefund());
        refundOrder.setPayOrderId("");
        refundOrder.setChannelPayOrderNo("");
        refundOrder.setRefundAmount(10l);

        refundOrder.setMchId("20001223");
        refundOrder.setChannelId("ALIPAY_PC");
        refundOrder.setChannelUser("jmdhappy@126.com");


        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("refundOrder", refundOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map map = payChannel4AliService.getAliRefundReq(jsonParam);
        System.out.println("map=" + map);
    }

    @Test
    public void testDoWxTransReq() {
        TransOrder transOrder = new TransOrder();
        transOrder.setTransOrderId(TransOrderId);
        transOrder.setMchId("20001222");
        transOrder.setChannelId("WX_JSAPI");
        transOrder.setChannelUser("oIkQuwhPgPUgl-TvQ48_UUpZUwMs");
        transOrder.setAmount(100l);
        transOrder.setUserName("丁志伟");
        transOrder.setRemarkInfo("测试XxPay转账");
        transOrder.setExtra("{\"checkName\":\"FORCE_CHECK\"}");  // 附加参数
        transOrder.setClientIp("210.73.211.141");
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("transOrder", transOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map map = payChannel4WxService.doWxTransReq(jsonParam);
        System.out.println("map=" + map);
    }
    //

    @Test
    public void testGetWxTransReq() {
        TransOrder transOrder = new TransOrder();
        transOrder.setTransOrderId("1509276544421");
        transOrder.setMchId("20001222");
        transOrder.setChannelId("WX_JSAPI");

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("transOrder", transOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map map = payChannel4WxService.getWxTransReq(jsonParam);
        System.out.println("map=" + map);
    }

    @Test
    public void testDoWxRefundReq() {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(MySeq.getRefund());
        refundOrder.setPayOrderId("P0020171029202216000002");
        //refundOrder.setChannelPayOrderNo("wx201710292022176ff41580340020277393");
        refundOrder.setRefundAmount(1l);
        refundOrder.setPayAmount(1l);
        refundOrder.setMchId("20001223");
        refundOrder.setChannelId("WX_JSAPI");
        refundOrder.setChannelUser("oIkQuwhPgPUgl-TvQ48_UUpZUwMs");


        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("refundOrder", refundOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map map = payChannel4WxService.doWxRefundReq(jsonParam);
        System.out.println("map=" + map);
    }

    @Test
    public void tesGetWxRefundReq() {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId("R0020171029202641000000");
        refundOrder.setPayOrderId("");
        refundOrder.setChannelPayOrderNo("");

        refundOrder.setMchId("20001223");
        refundOrder.setChannelId("WX_JSAPI");


        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("refundOrder", refundOrder);
        String jsonParam = RpcUtil.createBaseParam(paramMap);
        Map map = payChannel4WxService.getWxRefundReq(jsonParam);
        System.out.println("map=" + map);
    }

}
