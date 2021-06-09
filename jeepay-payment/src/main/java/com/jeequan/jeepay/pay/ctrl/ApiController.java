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
package com.jeequan.jeepay.pay.ctrl;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.pay.model.MchConfigContext;
import com.jeequan.jeepay.pay.rqrs.AbstractRQ;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.pay.service.ValidateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/*
* api 抽象接口， 公共函数
* 
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 17:28
*/
public abstract class ApiController extends AbstractCtrl {

    @Autowired private ValidateService validateService;
    @Autowired private ConfigContextService configContextService;


    /** 获取请求参数并转换为对象，通用验证  **/
    protected <T extends AbstractRQ> T getRQ(Class<T> cls){

        T bizRQ = getObject(cls);

        // [1]. 验证通用字段规则
        validateService.validate(bizRQ);

        return bizRQ;
    }


    /** 获取请求参数并转换为对象，商户通用验证  **/
    protected <T extends AbstractRQ> T getRQByWithMchSign(Class<T> cls){

        //获取请求RQ, and 通用验证
        T bizRQ = getRQ(cls);

        // 转换为 JSON
        JSONObject bizReqJSON = (JSONObject)JSONObject.toJSON(bizRQ);

        // [2]. 业务校验， 包括： 验签， 商户状态是否可用， 是否支持该支付方式下单等。
        String mchNo = bizReqJSON.getString("mchNo");
        String sign = bizRQ.getSign();

        if(StringUtils.isAnyEmpty(mchNo, sign)){
            throw new BizException("参数有误！");
        }

        MchConfigContext mchConfigContext = configContextService.getMchConfigContext(mchNo);

        MchInfo mchInfo = mchConfigContext == null ? null : mchConfigContext.getMchInfo();
        if(mchInfo == null || mchInfo.getState() != CS.YES){
            throw new BizException("商户不存在或商户状态不可用");
        }

        // 验签
        String privateKey = mchInfo.getPrivateKey();

        bizReqJSON.remove("sign");
        if(!sign.equalsIgnoreCase(JeepayKit.getSign(bizReqJSON, privateKey))){
             throw new BizException("验签失败");
        }

        return bizRQ;
    }
}
