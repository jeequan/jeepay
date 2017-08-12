package org.xxpay.service.channel.tencent.protocol.refund_query_protocol;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: rizenguo
 * Date: 2014/11/1
 * Time: 14:09
 * 用来存放退款订单数据
 */
public class RefundOrderData {

    private String outRefundNo = "";//商户退款单号
    private String refundID = "";//微信退款单号
    private String refundChannel = "";//退款渠道
//    IGINAL--原路退款
//    BALANCE--退回到余额
    private int refundFee = 0;//退款金额
    private int couponRefundFee = 0;//企业红包退款金额
    private String refundStatus = "";//退款状态

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public void setOutRefundNo(String outRefundNo) {
        this.outRefundNo = outRefundNo;
    }

    public String getRefundID() {
        return refundID;
    }

    public void setRefundID(String refundID) {
        this.refundID = refundID;
    }

    public String getRefundChannel() {
        return refundChannel;
    }

    public void setRefundChannel(String refundChannel) {
        this.refundChannel = refundChannel;
    }

    public int getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(int refundFee) {
        this.refundFee = refundFee;
    }

    public int getCouponRefundFee() {
        return couponRefundFee;
    }

    public void setCouponRefundFee(int couponRefundFee) {
        this.couponRefundFee = couponRefundFee;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String toMap(){
        Map<String,Object> map = new LinkedHashMap<String, Object>();
        Field[] fields = this.getClass().getDeclaredFields();
        StringBuilder s=new StringBuilder("{");
        
        for (Field field : fields) {
            Object obj;
            try {
                obj = field.get(this);
                if(obj!=null){
                	if(s.length()>0){
                		s.append(" ");
                	}
                	s.append(field.getName());
                	s.append("=");
                	s.append(obj.toString());
//                    map.put(field.getName(), obj);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        s.append("}");
        return s.toString();
    }

}
