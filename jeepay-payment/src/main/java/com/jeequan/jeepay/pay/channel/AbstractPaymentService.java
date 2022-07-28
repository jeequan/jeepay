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
package com.jeequan.jeepay.pay.channel;


import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.util.ChannelCertConfigKitBean;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/*
* 支付接口抽象类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:18
*/
public abstract class AbstractPaymentService implements IPaymentService{

    @Autowired protected SysConfigService sysConfigService;
    @Autowired protected ChannelCertConfigKitBean channelCertConfigKitBean;
    @Autowired protected ConfigContextQueryService configContextQueryService;

    /** 订单分账（一般用作 如微信订单将在下单处做标记） */
    protected boolean isDivisionOrder(PayOrder payOrder){
        //订单分账， 将冻结商户资金。
        if(payOrder.getDivisionMode() != null && (PayOrder.DIVISION_MODE_AUTO == payOrder.getDivisionMode() || PayOrder.DIVISION_MODE_MANUAL == payOrder.getDivisionMode() )){
            return true;
        }
        return false;
    }

    protected String getNotifyUrl(){
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/notify/" + getIfCode();
    }

    protected String getNotifyUrl(String payOrderId){
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/notify/" + getIfCode() + "/" + payOrderId;
    }

    protected String getReturnUrl(){
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/return/" + getIfCode();
    }

    protected String getReturnUrl(String payOrderId){
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/return/" + getIfCode() + "/" + payOrderId;
    }

    protected String getReturnUrlOnlyJump(String payOrderId){
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/pay/return/" + getIfCode() + "/" + CS.PAY_RETURNURL_FIX_ONLY_JUMP_PREFIX + payOrderId;
    }

}
