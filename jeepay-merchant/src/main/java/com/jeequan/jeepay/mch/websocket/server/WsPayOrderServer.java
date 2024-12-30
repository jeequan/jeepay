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
package com.jeequan.jeepay.mch.websocket.server;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/*
 * WebSocket服务类
 * /ws/payOrder/{訂單ID}/{客戶端自定義ID}
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/22 12:57
 */
@ServerEndpoint("/api/anon/ws/payOrder/{payOrderId}/{cid}")
@Component
public class WsPayOrderServer {

    private final static Logger logger = LoggerFactory.getLogger(WsPayOrderServer.class);

    //当前在线客户端 数量
    private static int onlineClientSize = 0;

    // payOrderId 与 WsPayOrderServer 存储关系, ConcurrentHashMap保证线程安全
    private static Map<String, Set<WsPayOrderServer>> wsOrderIdMap = new ConcurrentHashMap<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //客户端自定义ID
    private String cid = "";

    //支付订单号
    private String payOrderId = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("payOrderId") String payOrderId, @PathParam("cid") String cid) {

        try {
            //设置当前属性
            this.cid = cid;
            this.payOrderId = payOrderId;
            this.session = session;

            Set<WsPayOrderServer> wsServerSet = wsOrderIdMap.get(payOrderId);
            if(wsServerSet == null) {
                wsServerSet = new CopyOnWriteArraySet<>();
            }
            wsServerSet.add(this);
            wsOrderIdMap.put(payOrderId, wsServerSet);

            addOnlineCount(); //在线数加1
            logger.info("cid[{}],payOrderId[{}]连接开启监听！当前在线人数为{}", cid, payOrderId, onlineClientSize);

        } catch (Exception e) {
            logger.error("ws监听异常cid[{}],payOrderId[{}]", cid, payOrderId, e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {

        Set wsSet = wsOrderIdMap.get(this.payOrderId);
        wsSet.remove(this);
        if(wsSet.isEmpty()) {
            wsOrderIdMap.remove(this.payOrderId);
        }

        subOnlineCount(); //在线数减1
        logger.info("cid[{}],payOrderId[{}]连接关闭！当前在线人数为{}", cid, payOrderId, onlineClientSize);
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("ws发生错误", error);
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 根据订单ID,推送消息
     * 捕捉所有的异常，避免影响业务。
     * @param payOrderId
     */
    public static void sendMsgByOrderId(String payOrderId, String msg) {

        try {
            logger.info("推送ws消息到浏览器, payOrderId={}，msg={}", payOrderId, msg);


            Set<WsPayOrderServer> wsSet = wsOrderIdMap.get(payOrderId);
            if(wsSet == null || wsSet.isEmpty()){
                logger.info("payOrderId[{}] 无ws监听客户端", payOrderId);
                return ;
            }

            for (WsPayOrderServer item : wsSet) {
                try {
                    item.sendMessage(msg);
                } catch (Exception e) {
                    logger.info("推送设备消息时异常，payOrderId={}, cid={}", payOrderId, item.cid, e);
                    continue;
                }
            }
        } catch (Exception e) {
            logger.info("推送消息时异常，payOrderId={}", payOrderId, e);
        }
    }

    public static synchronized int getOnlineClientSize() {
        return onlineClientSize;
    }

    public static synchronized void addOnlineCount() {
        onlineClientSize++;
    }

    public static synchronized void subOnlineCount() {
        onlineClientSize--;
    }

}
