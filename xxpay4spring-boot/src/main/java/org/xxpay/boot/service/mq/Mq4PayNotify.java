package org.xxpay.boot.service.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.xxpay.common.util.MyLog;
import org.xxpay.boot.service.BaseService;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 业务通知MQ实现
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
public abstract class Mq4PayNotify extends BaseService {

    @Autowired
    private RestTemplate restTemplate;

    protected static final MyLog _log = MyLog.getLog(Mq4PayNotify.class);

    public abstract void send(String msg);

    /**
     * 发送延迟消息
     * @param msg
     * @param delay
     */
    public abstract void send(String msg, long delay);

    public void receive(String msg) {
        _log.info("do notify task, msg={}", msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String respUrl = msgObj.getString("url");
        String orderId = msgObj.getString("orderId");
        int count = msgObj.getInteger("count");
        if(StringUtils.isEmpty(respUrl)) {
            _log.warn("notify url is empty. respUrl={}", respUrl);
            return;
        }
        try {
        	String notifyResult = "";
            _log.info("==>MQ通知业务系统开始[orderId：{}][count：{}][time：{}]", orderId, count, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            try {
            	URI uri = new URI(respUrl);
                notifyResult = restTemplate.postForObject(uri, null, String.class);
            }catch (Exception e) {
				_log.error(e, "通知商户系统异常");
			}
            _log.info("<==MQ通知业务系统结束[orderId：{}][count：{}][time：{}]", orderId, count, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            // 验证结果
            _log.info("notify response , OrderID={}", orderId);
            if(notifyResult.trim().equalsIgnoreCase("success")){
                //_log.info("{} notify success, url:{}", _notifyInfo.getBusiId(), respUrl);
                //修改订单表
                try {
                    int result = super.baseUpdateStatus4Complete(orderId);
                    _log.info("修改payOrderId={},订单状态为处理完成->{}", orderId, result == 1 ? "成功" : "失败");
                } catch (Exception e) {
                    _log.error(e, "修改订单状态为处理完成异常");
                }
                // 修改通知次数
                try {
                    int result = super.baseUpdateNotify(orderId, (byte) 1);
                    _log.info("修改payOrderId={},通知业务系统次数->{}", orderId, result == 1 ? "成功" : "失败");
                }catch (Exception e) {
                    _log.error(e, "修改通知次数异常");
                }
                return ; // 通知成功结束
            }else {
                // 通知失败，延时再通知
                int cnt = count+1;
                _log.info("notify count={}", cnt);
                // 修改通知次数
                try {
                    int result = super.baseUpdateNotify(orderId, (byte) cnt);
                    _log.info("修改payOrderId={},通知业务系统次数->{}", orderId, result == 1 ? "成功" : "失败");
                }catch (Exception e) {
                    _log.error(e, "修改通知次数异常");
                }

                if (cnt > 5) {
                    _log.info("notify count>5 stop. url={}", respUrl);
                    return ;
                }
                msgObj.put("count", cnt);
                this.send(msgObj.toJSONString(), cnt * 60 * 1000);
            }
            _log.warn("notify failed. url:{}, response body:{}", respUrl, notifyResult.toString());
        } catch(Exception e) {
            _log.info("<==MQ通知业务系统结束[orderId：{}][count：{}][time：{}]", orderId, count, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            _log.error(e, "notify exception. url:%s", respUrl);
        }

    }
}
