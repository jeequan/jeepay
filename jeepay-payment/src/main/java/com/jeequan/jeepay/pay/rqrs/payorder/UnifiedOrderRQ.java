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
package com.jeequan.jeepay.pay.rqrs.payorder;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.rqrs.AbstractRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/*
* 创建订单请求参数对象
* 聚合支付接口（统一下单）
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 17:33
*/
@Data
public class UnifiedOrderRQ extends AbstractRQ {

    /** 商户号 **/
    @NotBlank(message="商户号不能为空")
    private String mchNo;

    /** 商户应用ID **/
    @NotBlank(message="商户应用ID不能为空")
    private String appId;

    /** 商户订单号 **/
    @NotBlank(message="商户订单号不能为空")
    private String mchOrderNo;

    /** 支付方式  如： wxpay_jsapi,alipay_wap等   **/
    @NotBlank(message="支付方式不能为空")
    private String wayCode;

    /** 支付金额， 单位：分 **/
    @NotNull(message="支付金额不能为空")
    @Min(value = 1, message = "支付金额不能为空")
    private Long amount;

    /** 货币代码 **/
    @NotBlank(message="货币代码不能为空")
    private String currency;

    /** 客户端IP地址 **/
    private String clientIp;

    /** 商品标题 **/
    @NotBlank(message="商品标题不能为空")
    private String subject;

    /** 商品描述信息 **/
    @NotBlank(message="商品描述信息不能为空")
    private String body;

    /** 异步通知地址 **/
    private String notifyUrl;

    /** 跳转通知地址 **/
    private String returnUrl;

    /** 订单失效时间 **/
    private String expiredTime;

    /** 特定渠道发起额外参数 **/
    private String channelExtra;

    /** 渠道用户标识,如微信openId,支付宝账号 **/
    private String channelUser;

    /** 商户扩展参数 **/
    private String extParam;


    /** 返回真实的bizRQ **/
    public UnifiedOrderRQ buildBizRQ(){

        if(CS.PAY_WAY_CODE.ALI_BAR.equals(wayCode)){
            AliBarOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliBarOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.ALI_JSAPI.equals(wayCode)){
            AliJsapiOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliJsapiOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.QR_CASHIER.equals(wayCode)){
            QrCashierOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), QrCashierOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.WX_JSAPI.equals(wayCode) || CS.PAY_WAY_CODE.WX_LITE.equals(wayCode)){
            WxJsapiOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), WxJsapiOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.WX_BAR.equals(wayCode)){
            WxBarOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), WxBarOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.WX_NATIVE.equals(wayCode)){
            WxNativeOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), WxNativeOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.WX_H5.equals(wayCode)){
            WxH5OrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), WxH5OrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.YSF_BAR.equals(wayCode)){
            YsfBarOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), YsfBarOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.YSF_JSAPI.equals(wayCode)){
            YsfJsapiOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), YsfJsapiOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.AUTO_BAR.equals(wayCode)){
            AutoBarOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AutoBarOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.ALI_APP.equals(wayCode)){
            AliAppOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliAppOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.ALI_WAP.equals(wayCode)){
            AliWapOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliWapOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.ALI_PC.equals(wayCode)){
            AliPcOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliPcOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.ALI_QR.equals(wayCode)){
            AliQrOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliQrOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }

        return this;
    }


}
