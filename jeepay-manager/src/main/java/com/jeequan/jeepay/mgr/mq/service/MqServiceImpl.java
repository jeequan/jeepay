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
package com.jeequan.jeepay.mgr.mq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * mq消息中转
 *
 * @author xiaoyu
 * @site https://www.jeepay.vip
 * @date 2021/6/25 17:10
 */
@Slf4j
@Service
public class MqServiceImpl {

    @Autowired private MqMchUserRemoveService mqMchUserRemoveService;
    @Autowired private MqPayOrderNotifyService mqPayOrderNotifyService;
    @Autowired private MqModifyIsvInfoService mqModifyIsvInfoService;
    @Autowired private MqModifyMchInfoService mqModifyMchInfoService;
    @Autowired private MqModifyMchAppService mqModifyMchAppService;
    @Autowired private MqModifySysConfigService mqModifySysConfigService;

    /** 删除商户用户信息 **/
    public void sendUserRemove(Collection<Long> userIdList){
        mqMchUserRemoveService.send(userIdList);
    }

    /** 订单回调信息 **/
    public void sendPayOrderNotify(String msg){
        mqPayOrderNotifyService.send(msg);
    }

    /** 服务商修改推送 **/
    public void sendModifyIsvInfo(String msg){ mqModifyIsvInfoService.send(msg); }

    /** 商户修改推送 **/
    public void sendModifyMchInfo(String msg){ mqModifyMchInfoService.send(msg); }

    /** 商户应用修改推送 **/
    public void sendModifyMchApp(String mchNo, String appId){
        mqModifyMchAppService.send(mchNo, appId);
    }

    /** 系统配置修改推送 **/
    public void sendModifySysConfig(String msg){
        mqModifySysConfigService.send(msg);
    }


}
