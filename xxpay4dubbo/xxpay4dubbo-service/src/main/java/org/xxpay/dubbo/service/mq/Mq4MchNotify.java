package org.xxpay.dubbo.service.mq;

import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import org.xxpay.common.util.MyLog;
import org.xxpay.dubbo.service.BaseService4PayOrder;

import javax.jms.*;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @Description: 商户通知MQ统一处理
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-10-31
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@Component
public class Mq4MchNotify extends BaseService4PayOrder {

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final MyLog _log = MyLog.getLog(Mq4MchNotify.class);

    public void send(Queue queue, String msg) {
        _log.info("发送MQ消息:msg={}", msg);
        this.jmsTemplate.convertAndSend(queue, msg);
    }

    /**
     * 发送延迟消息
     * @param msg
     * @param delay
     */
    public void send(Queue queue, String msg, long delay) {
        _log.info("发送MQ延时消息:msg={},delay={}", msg, delay);
        jmsTemplate.send(queue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage tm = session.createTextMessage(msg);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1*1000);
                tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
                return tm;
            }
        });
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
