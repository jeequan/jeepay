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
package com.jeequan.jeepay.core.model.params;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.model.params.alipay.AlipayNormalMchParams;
import com.jeequan.jeepay.core.model.params.plspay.PlspayNormalMchParams;
import com.jeequan.jeepay.core.model.params.pppay.PpPayNormalMchParams;
import com.jeequan.jeepay.core.model.params.wxpay.WxpayNormalMchParams;
import com.jeequan.jeepay.core.model.params.xxpay.XxpayNormalMchParams;

/*
 * 抽象类 普通商户参数定义
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 16:33
 */
public abstract class NormalMchParams {

    public static NormalMchParams factory(String ifCode, String paramsStr){

        if(CS.IF_CODE.WXPAY.equals(ifCode)){
            return JSONObject.parseObject(paramsStr, WxpayNormalMchParams.class);
        }else if(CS.IF_CODE.ALIPAY.equals(ifCode)){
            return JSONObject.parseObject(paramsStr, AlipayNormalMchParams.class);
        }else if(CS.IF_CODE.XXPAY.equals(ifCode)){
            return JSONObject.parseObject(paramsStr, XxpayNormalMchParams.class);
        }else if (CS.IF_CODE.PPPAY.equals(ifCode)){
            return JSONObject.parseObject(paramsStr, PpPayNormalMchParams.class);
        }else if (CS.IF_CODE.PLSPAY.equals(ifCode)){
            return JSONObject.parseObject(paramsStr, PlspayNormalMchParams.class);
        }
        return null;
    }

    /**
     *  敏感数据脱敏
     */
    public abstract String deSenData();

}
