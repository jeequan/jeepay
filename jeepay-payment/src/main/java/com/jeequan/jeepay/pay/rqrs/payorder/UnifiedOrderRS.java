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

import com.alibaba.fastjson.annotation.JSONField;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.Data;

/*
* 创建订单(统一订单) 响应参数
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:34
*/
@Data
public class UnifiedOrderRS extends AbstractRS {

    /** 支付订单号 **/
    private String payOrderId;

    /** 商户订单号 **/
    private String mchOrderNo;

    /** 订单状态 **/
    private Byte orderState;

    /** 支付参数类型  ( 无参数，  调起支付插件参数， 重定向到指定地址，  用户扫码   )   **/
    private String payDataType;

    /** 支付参数 **/
    private String payData;

    /** 渠道返回错误代码 **/
    private String errCode;

    /** 渠道返回错误信息 **/
    private String errMsg;

    /** 上游渠道返回数据包 (无需JSON序列化) **/
    @JSONField(serialize = false)
    private ChannelRetMsg channelRetMsg;

    /** 生成聚合支付参数 (仅统一下单接口使用) **/
    public String buildPayDataType(){
        return CS.PAY_DATA_TYPE.NONE;
    }

    /** 生成支付参数 **/
    public String buildPayData(){
        return "";
    }


}
