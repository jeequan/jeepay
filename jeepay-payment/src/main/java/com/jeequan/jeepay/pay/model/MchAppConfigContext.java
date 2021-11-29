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
package com.jeequan.jeepay.pay.model;

import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.model.params.IsvsubMchParams;
import com.jeequan.jeepay.core.model.params.NormalMchParams;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/*
* 商户应用支付参数信息
* 放置到内存， 避免多次查询操作
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:29
*/
@Data
public class MchAppConfigContext {


    /** 商户信息缓存 */
    private String mchNo;
    private String appId;
    private Byte mchType;
    private MchInfo mchInfo;
    private MchApp mchApp;

    /** 商户支付配置信息缓存,  <接口代码, 支付参数>  */
    private Map<String, NormalMchParams> normalMchParamsMap = new HashMap<>();
    private Map<String, IsvsubMchParams> isvsubMchParamsMap = new HashMap<>();

    /** 放置所属服务商的信息 **/
    private IsvConfigContext isvConfigContext;

    /** 缓存 Paypal 对象 **/
    private PaypalWrapper paypalWrapper;

    /** 缓存支付宝client 对象 **/
    private AlipayClientWrapper alipayClientWrapper;

    /** 缓存 wxServiceWrapper 对象 **/
    private WxServiceWrapper wxServiceWrapper;

    /** 获取普通商户配置信息 **/
    public NormalMchParams getNormalMchParamsByIfCode(String ifCode){
        return normalMchParamsMap.get(ifCode);
    }

    /** 获取isv配置信息 **/
    public <T> T getNormalMchParamsByIfCode(String ifCode, Class<? extends NormalMchParams> cls){
        return (T)normalMchParamsMap.get(ifCode);
    }

    /** 获取特约商户配置信息 **/
    public IsvsubMchParams getIsvsubMchParamsByIfCode(String ifCode){
        return isvsubMchParamsMap.get(ifCode);
    }

    /** 获取isv配置信息 **/
    public <T> T getIsvsubMchParamsByIfCode(String ifCode, Class<? extends IsvsubMchParams> cls){
        return (T)isvsubMchParamsMap.get(ifCode);
    }

    /** 是否为 服务商特约商户 **/
    public boolean isIsvsubMch(){
        return this.mchType == MchInfo.TYPE_ISVSUB;
    }

    public AlipayClientWrapper getAlipayClientWrapper(){
        return isIsvsubMch() ? isvConfigContext.getAlipayClientWrapper(): alipayClientWrapper;
    }

    public WxServiceWrapper getWxServiceWrapper(){
        return isIsvsubMch() ? isvConfigContext.getWxServiceWrapper(): wxServiceWrapper;
    }

}
