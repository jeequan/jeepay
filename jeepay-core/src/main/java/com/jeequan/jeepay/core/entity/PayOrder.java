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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 支付订单表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Schema(description = "支付订单表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_order")
public class PayOrder extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<PayOrder> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;


    public static final byte STATE_INIT = 0; //订单生成
    public static final byte STATE_ING = 1; //支付中
    public static final byte STATE_SUCCESS = 2; //支付成功
    public static final byte STATE_FAIL = 3; //支付失败
    public static final byte STATE_CANCEL = 4; //已撤销
    public static final byte STATE_REFUND = 5; //已退款
    public static final byte STATE_CLOSED = 6; //订单关闭

    public static final byte REFUND_STATE_NONE = 0; //未发生实际退款
    public static final byte REFUND_STATE_SUB = 1; //部分退款
    public static final byte REFUND_STATE_ALL = 2; //全额退款


    public static final byte DIVISION_MODE_FORBID = 0; //该笔订单不允许分账
    public static final byte DIVISION_MODE_AUTO = 1; //支付成功按配置自动完成分账
    public static final byte DIVISION_MODE_MANUAL = 2; //商户手动分账(解冻商户金额)

    public static final byte DIVISION_STATE_UNHAPPEN = 0; //未发生分账
    public static final byte DIVISION_STATE_WAIT_TASK = 1; //等待分账任务处理
    public static final byte DIVISION_STATE_ING = 2; //分账处理中
    public static final byte DIVISION_STATE_FINISH = 3; //分账任务已结束(不体现状态)


    /**
     * 支付订单号
     */
    @Schema(title = "payOrderId", description = "支付订单号")
    @TableId
    private String payOrderId;

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
     * 商户订单号
     */
    @Schema(title = "mchOrderNo", description = "商户订单号")
    private String mchOrderNo;

    /**
     * 支付接口代码
     */
    @Schema(title = "ifCode", description = "支付接口代码")
    private String ifCode;

    /**
     * 支付方式代码
     */
    @Schema(title = "wayCode", description = "支付方式代码")
    private String wayCode;

    /**
     * 支付金额,单位分
     */
    @Schema(title = "amount", description = "支付金额,单位分")
    private Long amount;

    /**
     * 商户手续费费率快照
     */
    @Schema(title = "mchFeeRate", description = "商户手续费费率快照")
    private BigDecimal mchFeeRate;

    /**
     * 商户手续费,单位分
     */
    @Schema(title = "mchFeeAmount", description = "商户手续费,单位分")
    private Long mchFeeAmount;

    /**
     * 三位货币代码,人民币:cny
     */
    @Schema(title = "currency", description = "三位货币代码,人民币:cny")
    private String currency;

    /**
     * 支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭
     */
    @Schema(title = "state", description = "支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭")
    private Byte state;

    /**
     * 向下游回调状态, 0-未发送,  1-已发送
     */
    @Schema(title = "notifyState", description = "向下游回调状态, 0-未发送,  1-已发送")
    private Byte notifyState;

    /**
     * 客户端IP
     */
    @Schema(title = "clientIp", description = "客户端IP")
    private String clientIp;

    /**
     * 商品标题
     */
    @Schema(title = "subject", description = "商品标题")
    private String subject;

    /**
     * 商品描述信息
     */
    @Schema(title = "body", description = "商品描述信息")
    private String body;

    /**
     * 特定渠道发起额外参数
     */
    @Schema(title = "channelExtra", description = "特定渠道发起额外参数")
    private String channelExtra;

    /**
     * 渠道用户标识,如微信openId,支付宝账号
     */
    @Schema(title = "channelUser", description = "渠道用户标识,如微信openId,支付宝账号")
    private String channelUser;

    /**
     * 渠道订单号
     */
    @Schema(title = "channelOrderNo", description = "渠道订单号")
    private String channelOrderNo;

    /**
     * 退款状态: 0-未发生实际退款, 1-部分退款, 2-全额退款
     */
    @Schema(title = "refundState", description = "退款状态: 0-未发生实际退款, 1-部分退款, 2-全额退款")
    private Byte refundState;

    /**
     * 退款次数
     */
    @Schema(title = "refundTimes", description = "退款次数")
    private Integer refundTimes;

    /**
     * 退款总金额,单位分
     */
    @Schema(title = "refundAmount", description = "退款总金额,单位分")
    private Long refundAmount;

    /**
     * 订单分账模式：0-该笔订单不允许分账, 1-支付成功按配置自动完成分账, 2-商户手动分账(解冻商户金额)
     */
    @Schema(title = "divisionMode", description = "订单分账模式：0-该笔订单不允许分账, 1-支付成功按配置自动完成分账, 2-商户手动分账(解冻商户金额)")
    private Byte divisionMode;

    /**
     * 0-未发生分账, 1-等待分账任务处理, 2-分账处理中, 3-分账任务已结束(不体现状态)
     */
    @Schema(title = "divisionState", description = "0-未发生分账, 1-等待分账任务处理, 2-分账处理中, 3-分账任务已结束(不体现状态)")
    private Byte divisionState;

    /**
     * 最新分账时间
     */
    @Schema(title = "divisionLastTime", description = "最新分账时间")
    private Date divisionLastTime;

    /**
     * 渠道支付错误码
     */
    @Schema(title = "errCode", description = "渠道支付错误码")
    private String errCode;

    /**
     * 渠道支付错误描述
     */
    @Schema(title = "errMsg", description = "渠道支付错误描述")
    private String errMsg;

    /**
     * 商户扩展参数
     */
    @Schema(title = "extParam", description = "商户扩展参数")
    private String extParam;

    /**
     * 异步通知地址
     */
    @Schema(title = "notifyUrl", description = "异步通知地址")
    private String notifyUrl;

    /**
     * 页面跳转地址
     */
    @Schema(title = "returnUrl", description = "页面跳转地址")
    private String returnUrl;

    /**
     * 订单失效时间
     */
    @Schema(title = "expiredTime", description = "订单失效时间")
    private Date expiredTime;

    /**
     * 订单支付成功时间
     */
    @Schema(title = "successTime", description = "订单支付成功时间")
    private Date successTime;

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
