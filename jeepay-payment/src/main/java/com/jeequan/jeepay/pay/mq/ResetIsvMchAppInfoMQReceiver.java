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
package com.jeequan.jeepay.pay.mq;

import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 接收MQ消息
 * 业务： 更新服务商/商户/商户应用配置信息；
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/7/27 9:23
 */
@Slf4j
@Component
public class ResetIsvMchAppInfoMQReceiver implements ResetIsvMchAppInfoConfigMQ.IMQReceiver {

    @Autowired
    private ConfigContextService configContextService;

    @Override
    public void receive(ResetIsvMchAppInfoConfigMQ.MsgPayload payload) {

        if(payload.getResetType() == ResetIsvMchAppInfoConfigMQ.RESET_TYPE_ISV_INFO){
            this.modifyIsvInfo(payload.getIsvNo());
        }else if(payload.getResetType() == ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_INFO){
            this.modifyMchInfo(payload.getMchNo());
        }else if(payload.getResetType() == ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_APP){
            this.modifyMchApp(payload.getMchNo(), payload.getAppId());
        }

    }

    /** 接收 [商户配置信息] 的消息 **/
    private void modifyMchInfo(String mchNo) {
        log.info("成功接收 [商户配置信息] 的消息, msg={}", mchNo);
        configContextService.initMchInfoConfigContext(mchNo);
        log.info(" [商户配置信息] 已重置");
    }

    /** 接收 [商户应用支付参数配置信息] 的消息 **/
    private void modifyMchApp(String mchNo, String appId) {
        log.info("成功接收 [商户应用支付参数配置信息] 的消息, mchNo={}, appId={}", mchNo, appId);
        configContextService.initMchAppConfigContext(mchNo, appId);
        log.info(" [商户应用支付参数配置信息] 已重置");
    }

    /** 重置ISV信息 **/
    private void modifyIsvInfo(String isvNo) {
        log.info("成功接收 [ISV信息] 重置, msg={}", isvNo);
        configContextService.initIsvConfigContext(isvNo);
        log.info("[ISV信息] 已重置");
    }

}
