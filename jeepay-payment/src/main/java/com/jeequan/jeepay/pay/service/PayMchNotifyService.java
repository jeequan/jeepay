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
package com.jeequan.jeepay.pay.service;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.model.PayOrderMchNotifyMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.pay.rqrs.payorder.QueryPayOrderRS;
import com.jeequan.jeepay.pay.rqrs.refund.QueryRefundOrderRS;
import com.jeequan.jeepay.pay.rqrs.transfer.QueryTransferOrderRS;
import com.jeequan.jeepay.service.impl.MchNotifyRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
* 商户通知 service
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:43
*/
@Slf4j
@Service
public class PayMchNotifyService {

    @Autowired private MchNotifyRecordService mchNotifyRecordService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private IMQSender mqSender;


    /** 商户通知信息， 只有订单是终态，才会发送通知， 如明确成功和明确失败 **/
    public void payOrderNotify(PayOrder dbPayOrder){

        try {
            // 通知地址为空
            if(StringUtils.isEmpty(dbPayOrder.getNotifyUrl())){
                return ;
            }

            //获取到通知对象
            MchNotifyRecord mchNotifyRecord = mchNotifyRecordService.findByPayOrder(dbPayOrder.getPayOrderId());

            if(mchNotifyRecord != null){

                log.info("当前已存在通知消息， 不再发送。");
                return ;
            }

            //商户app私钥
            String appSecret = configContextQueryService.queryMchApp(dbPayOrder.getMchNo(), dbPayOrder.getAppId()).getAppSecret();

            // 封装通知url
            String notifyUrl = createNotifyUrl(dbPayOrder, appSecret);
            mchNotifyRecord = new MchNotifyRecord();
            mchNotifyRecord.setOrderId(dbPayOrder.getPayOrderId());
            mchNotifyRecord.setOrderType(MchNotifyRecord.TYPE_PAY_ORDER);
            mchNotifyRecord.setMchNo(dbPayOrder.getMchNo());
            mchNotifyRecord.setMchOrderNo(dbPayOrder.getMchOrderNo()); //商户订单号
            mchNotifyRecord.setIsvNo(dbPayOrder.getIsvNo());
            mchNotifyRecord.setAppId(dbPayOrder.getAppId());
            mchNotifyRecord.setNotifyUrl(notifyUrl);
            mchNotifyRecord.setResResult("");
            mchNotifyRecord.setNotifyCount(0);
            mchNotifyRecord.setState(MchNotifyRecord.STATE_ING); // 通知中

            try {
                mchNotifyRecordService.save(mchNotifyRecord);
            } catch (Exception e) {
                log.info("数据库已存在[{}]消息，本次不再推送。", mchNotifyRecord.getOrderId());
                return ;
            }

            //推送到MQ
            Long notifyId = mchNotifyRecord.getNotifyId();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception e) {
            log.error("推送失败！", e);
        }
    }

    /** 商户通知信息，退款成功的发送通知 **/
    public void refundOrderNotify(RefundOrder dbRefundOrder){

        try {
            // 通知地址为空
            if(StringUtils.isEmpty(dbRefundOrder.getNotifyUrl())){
                return ;
            }

            //获取到通知对象
            MchNotifyRecord mchNotifyRecord = mchNotifyRecordService.findByRefundOrder(dbRefundOrder.getRefundOrderId());

            if(mchNotifyRecord != null){

                log.info("当前已存在通知消息， 不再发送。");
                return ;
            }

            //商户app私钥
            String appSecret = configContextQueryService.queryMchApp(dbRefundOrder.getMchNo(), dbRefundOrder.getAppId()).getAppSecret();

            // 封装通知url
            String notifyUrl = createNotifyUrl(dbRefundOrder, appSecret);
            mchNotifyRecord = new MchNotifyRecord();
            mchNotifyRecord.setOrderId(dbRefundOrder.getRefundOrderId());
            mchNotifyRecord.setOrderType(MchNotifyRecord.TYPE_REFUND_ORDER);
            mchNotifyRecord.setMchNo(dbRefundOrder.getMchNo());
            mchNotifyRecord.setMchOrderNo(dbRefundOrder.getMchRefundNo()); //商户订单号
            mchNotifyRecord.setIsvNo(dbRefundOrder.getIsvNo());
            mchNotifyRecord.setAppId(dbRefundOrder.getAppId());
            mchNotifyRecord.setNotifyUrl(notifyUrl);
            mchNotifyRecord.setResResult("");
            mchNotifyRecord.setNotifyCount(0);
            mchNotifyRecord.setState(MchNotifyRecord.STATE_ING); // 通知中

            try {
                mchNotifyRecordService.save(mchNotifyRecord);
            } catch (Exception e) {
                log.info("数据库已存在[{}]消息，本次不再推送。", mchNotifyRecord.getOrderId());
                return ;
            }

            //推送到MQ
            Long notifyId = mchNotifyRecord.getNotifyId();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception e) {
            log.error("推送失败！", e);
        }
    }


    /** 商户通知信息，转账订单的通知接口 **/
    public void transferOrderNotify(TransferOrder dbTransferOrder){

        try {
            // 通知地址为空
            if(StringUtils.isEmpty(dbTransferOrder.getNotifyUrl())){
                return ;
            }

            //获取到通知对象
            MchNotifyRecord mchNotifyRecord = mchNotifyRecordService.findByTransferOrder(dbTransferOrder.getTransferId());

            if(mchNotifyRecord != null){
                log.info("当前已存在通知消息， 不再发送。");
                return ;
            }

            //商户app私钥
            String appSecret = configContextQueryService.queryMchApp(dbTransferOrder.getMchNo(), dbTransferOrder.getAppId()).getAppSecret();

            // 封装通知url
            String notifyUrl = createNotifyUrl(dbTransferOrder, appSecret);
            mchNotifyRecord = new MchNotifyRecord();
            mchNotifyRecord.setOrderId(dbTransferOrder.getTransferId());
            mchNotifyRecord.setOrderType(MchNotifyRecord.TYPE_TRANSFER_ORDER);
            mchNotifyRecord.setMchNo(dbTransferOrder.getMchNo());
            mchNotifyRecord.setMchOrderNo(dbTransferOrder.getMchOrderNo()); //商户订单号
            mchNotifyRecord.setIsvNo(dbTransferOrder.getIsvNo());
            mchNotifyRecord.setAppId(dbTransferOrder.getAppId());
            mchNotifyRecord.setNotifyUrl(notifyUrl);
            mchNotifyRecord.setResResult("");
            mchNotifyRecord.setNotifyCount(0);
            mchNotifyRecord.setState(MchNotifyRecord.STATE_ING); // 通知中

            try {
                mchNotifyRecordService.save(mchNotifyRecord);
            } catch (Exception e) {
                log.info("数据库已存在[{}]消息，本次不再推送。", mchNotifyRecord.getOrderId());
                return ;
            }

            //推送到MQ
            Long notifyId = mchNotifyRecord.getNotifyId();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception e) {
            log.error("推送失败！", e);
        }
    }


    /**
     * 创建响应URL
     */
    public String createNotifyUrl(PayOrder payOrder, String appSecret) {

        QueryPayOrderRS queryPayOrderRS = QueryPayOrderRS.buildByPayOrder(payOrder);
        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(queryPayOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", JeepayKit.getSign(jsonObject, appSecret));

        // 生成通知
        return StringKit.appendUrlQuery(payOrder.getNotifyUrl(), jsonObject);
    }


    /**
     * 创建响应URL
     */
    public String createNotifyUrl(RefundOrder refundOrder, String appSecret) {

        QueryRefundOrderRS queryRefundOrderRS = QueryRefundOrderRS.buildByRefundOrder(refundOrder);
        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(queryRefundOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", JeepayKit.getSign(jsonObject, appSecret));

        // 生成通知
        return StringKit.appendUrlQuery(refundOrder.getNotifyUrl(), jsonObject);
    }


    /**
     * 创建响应URL
     */
    public String createNotifyUrl(TransferOrder transferOrder, String appSecret) {

        QueryTransferOrderRS rs = QueryTransferOrderRS.buildByRecord(transferOrder);
        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(rs);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", JeepayKit.getSign(jsonObject, appSecret));

        // 生成通知
        return StringKit.appendUrlQuery(transferOrder.getNotifyUrl(), jsonObject);
    }


    /**
     * 创建响应URL
     */
    public String createReturnUrl(PayOrder payOrder, String appSecret) {

        if(StringUtils.isEmpty(payOrder.getReturnUrl())){
            return "";
        }

        QueryPayOrderRS queryPayOrderRS = QueryPayOrderRS.buildByPayOrder(payOrder);
        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(queryPayOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", JeepayKit.getSign(jsonObject, appSecret));   // 签名

        // 生成跳转地址
        return StringKit.appendUrlQuery(payOrder.getReturnUrl(), jsonObject);

    }

}
