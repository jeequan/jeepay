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
package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 * 退款订单表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Schema(description = "退款订单表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_refund_order")
public class RefundOrder extends BaseModel {

    public static final byte STATE_INIT = 0; //订单生成
    public static final byte STATE_ING = 1; //退款中
    public static final byte STATE_SUCCESS = 2; //退款成功
    public static final byte STATE_FAIL = 3; //退款失败
    public static final byte STATE_CLOSED = 4; //退款任务关闭

    public static final LambdaQueryWrapper<RefundOrder> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    /**
     * 退款订单号（支付系统生成订单号）
     */
    @Schema(title = "refundOrderId", description = "退款订单号（支付系统生成订单号）")
    @TableId
    private String refundOrderId;

    /**
     * 支付订单号（与t_pay_order对应）
     */
    @Schema(title = "payOrderId", description = "支付订单号（与t_pay_order对应）")
    private String payOrderId;

    /**
     * 渠道支付单号（与t_pay_order channel_order_no对应）
     */
    @Schema(title = "channelPayOrderNo", description = "渠道支付单号（与t_pay_order channel_order_no对应）")
    private String channelPayOrderNo;

    /**
     * 商户号
     */
    @Schema(title = "mchNo", description = "商户号")
    private String mchNo;

    /**
     * 服务商号
     */
    @Schema(title = "isvNo", description = "服务商号")
    private String isvNo;

    /**
     * 应用ID
     */
    @Schema(title = "appId", description = "应用ID")
    private String appId;

    /**
     * 商户名称
     */
    @Schema(title = "mchName", description = "商户名称")
    private String mchName;

    /**
     * 类型: 1-普通商户, 2-特约商户(服务商模式)
     */
    @Schema(title = "mchType", description = "类型: 1-普通商户, 2-特约商户(服务商模式)")
    private Byte mchType;

    /**
     * 商户退款单号（商户系统的订单号）
     */
    @Schema(title = "mchRefundNo", description = "商户退款单号（商户系统的订单号）")
    private String mchRefundNo;

    /**
     * 支付方式代码
     */
    @Schema(title = "wayCode", description = "支付方式代码")
    private String wayCode;

    /**
     * 支付接口代码
     */
    @Schema(title = "ifCode", description = "支付接口代码")
    private String ifCode;

    /**
     * 支付金额,单位分
     */
    @Schema(title = "payAmount", description = "支付金额,单位分")
    private Long payAmount;

    /**
     * 退款金额,单位分
     */
    @Schema(title = "refundAmount", description = "退款金额,单位分")
    private Long refundAmount;

    /**
     * 三位货币代码,人民币:cny
     */
    @Schema(title = "currency", description = "三位货币代码,人民币:cny")
    private String currency;

    /**
     * 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-退款任务关闭
     */
    @Schema(title = "state", description = "退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-退款任务关闭")
    private Byte state;

    /**
     * 客户端IP
     */
    @Schema(title = "clientIp", description = "客户端IP")
    private String clientIp;

    /**
     * 退款原因
     */
    @Schema(title = "refundReason", description = "退款原因")
    private String refundReason;

    /**
     * 渠道订单号
     */
    @Schema(title = "channelOrderNo", description = "渠道订单号")
    private String channelOrderNo;

    /**
     * 渠道错误码
     */
    @Schema(title = "errCode", description = "渠道错误码")
    private String errCode;

    /**
     * 渠道错误描述
     */
    @Schema(title = "errMsg", description = "渠道错误描述")
    private String errMsg;

    /**
     * 特定渠道发起时额外参数
     */
    @Schema(title = "channelExtra", description = "特定渠道发起时额外参数")
    private String channelExtra;

    /**
     * 通知地址
     */
    @Schema(title = "notifyUrl", description = "通知地址")
    private String notifyUrl;

    /**
     * 扩展参数
     */
    @Schema(title = "extParam", description = "扩展参数")
    private String extParam;

    /**
     * 订单退款成功时间
     */
    @Schema(title = "successTime", description = "订单退款成功时间")
    private Date successTime;

    /**
     * 退款失效时间（失效后系统更改为退款任务关闭状态）
     */
    @Schema(title = "expiredTime", description = "退款失效时间（失效后系统更改为退款任务关闭状态）")
    private Date expiredTime;

    /**
     * 创建时间
     */
    @Schema(title = "createdAt", description = "创建时间")
    private Date createdAt;

    /**
     * 更新时间
     */
    @Schema(title = "updatedAt", description = "更新时间")
    private Date updatedAt;

}
