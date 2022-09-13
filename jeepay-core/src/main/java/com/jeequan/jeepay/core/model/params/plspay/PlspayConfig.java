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
package com.jeequan.jeepay.core.model.params.plspay;

import lombok.Data;

/*
 * 计全支付plus， 通用配置信息
 *
 * @author yurong
 * @site https://www.jeequan.com
 * @date 2022/8/11 14:32
 */
@Data
public class PlspayConfig {

    /** 签名类型 */
    public static final String DEFAULT_SIGN_TYPE = "MD5";
    public static final String SIGN_TYPE_RSA2 = "RSA2";

    /** 支付订单状态 */
    public static String PAY_STATE_SUCCESS = "2";    // 2-支付成功
    public static String PAY_STATE_FAIL = "3";       // 3-支付失败

    /** 退款订单状态 */
    public static String REFUND_STATE_SUCCESS = "2";    // 2-退款成功
    public static String REFUND_STATE_FAIL = "3";       // 3-退款失败

    /** 支付方式 */
    public static String ALI_BAR = "ALI_BAR";               // 支付宝条码
    public static String ALI_JSAPI = "ALI_JSAPI";           // 支付宝生活号
    public static String ALI_LITE = "ALI_LITE";             // 支付宝小程序
    public static String ALI_APP = "ALI_APP";               // 支付宝APP
    public static String ALI_WAP = "ALI_WAP";               // 支付宝WAP
    public static String ALI_PC = "ALI_PC";                 // 支付宝PC网站
    public static String ALI_QR = "ALI_QR";                 // 支付宝二维码
    public static String WX_BAR = "WX_BAR";                 // 微信条码
    public static String WX_JSAPI = "WX_JSAPI";             // 微信公众号
    public static String WX_LITE = "WX_LITE";               // 微信小程序
    public static String WX_APP = "WX_APP";                 // 微信APP
    public static String WX_H5 = "WX_H5";                   // 微信H5
    public static String WX_NATIVE = "WX_NATIVE";           // 微信扫码
}