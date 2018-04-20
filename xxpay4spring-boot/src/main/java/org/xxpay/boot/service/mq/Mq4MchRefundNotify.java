package org.xxpay.boot.service.mq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xxpay.boot.service.BaseService;
import org.xxpay.common.util.MyLog;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @Description: 商户通知MQ统一处理
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-10-31
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@Component
public abstract class Mq4MchRefundNotify extends BaseService {


    protected static final MyLog _log = MyLog.getLog(Mq4MchRefundNotify.class);

    public abstract void send(String msg);
    /**
     * 发送延迟消息
     * @param msg
     * @param delay
     */
    public abstract void send(String msg, long delay);

    @JmsListener(destination = MqConfig.MCH_REFUND_NOTIFY_QUEUE_NAME)
    public void receive(String msg) {
        String logPrefix = "【商户退款通知】";
        _log.info("{}接收消息:msg={}", logPrefix, msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String respUrl = msgObj.getString("url");
        String orderId = msgObj.getString("orderId");
        int count = msgObj.getInteger("count");
        if(StringUtils.isEmpty(respUrl)) {
            _log.warn("{}商户通知URL为空,respUrl={}", logPrefix, respUrl);
            return;
        }
        String httpResult = httpPost(respUrl);
        int cnt = count + 1;
        _log.info("{}notifyCount={}", logPrefix, cnt);
        if("success".equalsIgnoreCase(httpResult)){
            // 修改退款订单表
            try {
                int result = baseUpdateStatus4CompleteByRefund(orderId);
                _log.info("{}修改payOrderId={},退款订单状态为处理完成->{}", logPrefix, orderId, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error(e, "修改订单状态为处理完成异常");
            }
            // 修改通知
            try {
                int result = super.baseUpdateMchNotifySuccess(orderId, httpResult, (byte) cnt);
                _log.info("{}订单退款修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, cnt, result == 1 ? "成功" : "失败");
            }catch (Exception e) {
                _log.error(e, "订单退款修改商户支付通知异常");
            }
            return ; // 通知成功结束
        }else {
            // 修改通知次数
            try {
                int result = super.baseUpdateMchNotifyFail(orderId, httpResult, (byte) cnt);
                _log.info("{}订单退款修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, cnt, result == 1 ? "成功" : "失败");
            }catch (Exception e) {
                _log.error(e, "订单退款修改商户支付通知异常");
            }
            if (cnt > 5) {
                _log.info("{}订单退款通知次数notifyCount()>5,停止通知", respUrl, cnt);
                return ;
            }
            // 通知失败，延时再通知
            msgObj.put("count", cnt);
            this.send(msgObj.toJSONString(), cnt * 60 * 1000);
            _log.info("{}发送延时通知完成,通知次数:{},{}秒后执行通知", respUrl, cnt, cnt * 60);
        }
    }
    
    private static class TrustAnyTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }

    public String httpPost(String url) {
        StringBuffer sb = new StringBuffer();
        try {
            URL console = new URL(url);
            if("https".equals(console.getProtocol())) {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },
                        new java.security.SecureRandom());
                HttpsURLConnection con = (HttpsURLConnection) console.openConnection();
                con.setSSLSocketFactory(sc.getSocketFactory());
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setConnectTimeout(30 * 1000);
                con.setReadTimeout(60 * 1000);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()), 1024*1024);
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                }
                in.close();
            }else if("http".equals(console.getProtocol())) {
                HttpURLConnection con = (HttpURLConnection) console.openConnection();
                con.setRequestMethod("POST");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setConnectTimeout(30 * 1000);
                con.setReadTimeout(60 * 1000);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()), 1024*1024);
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                }
                in.close();
            }else {
                _log.error("not do protocol. protocol=%s", console.getProtocol());
            }
        } catch(Exception e) {
            _log.error(e, "httpPost exception. url:%s", url);
        }
        return sb.toString();
    }
}
