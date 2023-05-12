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
package com.jeequan.jeepay.pay.channel.wxpay.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

/**
*
* 微信V3支付请求参数类
* @author terrfly
* @site https://www.jeequan.com
* @date 2022/4/25 11:52
*/
@Data
@Accessors(chain = true)
public class WxpayV3OrderRequestModel {

    /** 服务商应用ID */
    @JSONField(name = "sp_appid")
    private String spAppid;

    /** 服务商户号 */
    @JSONField(name = "sp_mchid")
    private String spMchid;

    /** 子商户号 */
    @JSONField(name = "sub_mchid")
    private String subMchid;

    /** 子商户应用ID */
    @JSONField(name = "sub_appid")
    private String subAppid;

    /** 普通商户： 商户号 */
    @JSONField(name = "mchid")
    private String normalMchid;

    /** 普通商户： appId */
    @JSONField(name = "appid")
    private String normalAppid;

    /** 商户订单号 */
    @JSONField(name = "out_trade_no")
    private String outTradeNo;

    /** 商品描述 */
    private String description;

    /** 交易结束时间 */
    @JSONField(name = "time_expire")
    private String timeExpire;

    /** 附加数据 */
    private String attach;

    /** 通知地址 */
    @JSONField(name = "notify_url")
    private String notifyUrl;

    /** 订单优惠标记 */
    @JSONField(name = "goods_tag")
    private String goodsTag;

    /** 结算信息 */
    @JSONField(name = "settle_info")
    private SettleInfo settleInfo;

    /** 订单金额 */
    private Amount amount;

    /** 支付者 */
    private Payer payer;

    /** 场景信息 */
    @JSONField(name = "scene_info")
    private SceneInfo sceneInfo;

    /**  场景信息 **/
    @Data
    @Accessors(chain = true)
    public static class SceneInfo{

        /** 用户终端IP */
        @JSONField(name = "payer_client_ip")
        private String payerClientIp;

        /** 商户端设备号 */
        @JSONField(name = "device_id")
        private String deviceId;

        /** 商户端设备号 */
        @JSONField(name = "h5_info")
        private H5Info h5Info;

        /** H5场景信息 */
        @Data
        @Accessors(chain = true)
        public static class H5Info{

            /** 场景类型 */
            @JSONField(name = "type")
            private String type;

        }

    }


    /** 结算信息 **/
    @Data
    @Accessors(chain = true)
    public static class SettleInfo{

        /** 用户服务标识 */
        @JSONField(name = "profit_sharing")
        private Boolean profitSharing;

    }


    /** 支付者 **/
    @Data
    @Accessors(chain = true)
    public static class Payer{

        /** 普通商户的： 用户服务标识 */
        @JSONField(name = "openid")
        private String normalOpenId;

        /** 用户服务标识 */
        @JSONField(name = "sp_openid")
        private String spOpenid;

        /** 用户子标识 */
        @JSONField(name = "sub_openid")
        private String subOpenid;
    }

    /** 订单金额 **/
    @Data
    @Accessors(chain = true)
    public static class Amount{

        /** 总金额 */
        private Integer total;

        /** 货币类似 */
        private String currency;
    }

}
