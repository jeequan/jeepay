package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 商户分账接收者账号绑定关系表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-08-19
 */
@Schema(description = "商户分账接收者账号绑定关系表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_division_receiver")
public class MchDivisionReceiver implements Serializable {

    private static final long serialVersionUID=1L;

    //gw
    public static final LambdaQueryWrapper<MchDivisionReceiver> gw(){
        return new LambdaQueryWrapper<>();
    }

    /**
     * 分账接收者ID
     */
    @Schema(title = "receiverId", description = "分账接收者ID")
    @TableId(value = "receiver_id", type = IdType.AUTO)
    private Long receiverId;

    /**
     * 接收者账号别名
     */
    @Schema(title = "receiverAlias", description = "接收者账号别名")
    private String receiverAlias;

    /**
     * 组ID（便于商户接口使用）
     */
    @Schema(title = "receiverGroupId", description = "组ID（便于商户接口使用）")
    private Long receiverGroupId;

    /**
     * 组名称
     */
    @Schema(title = "receiverGroupName", description = "组名称")
    private String receiverGroupName;

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
     * 支付接口代码
     */
    @Schema(title = "ifCode", description = "支付接口代码")
    private String ifCode;

    /**
     * 分账接收账号类型: 0-个人(对私) 1-商户(对公)
     */
    @Schema(title = "accType", description = "分账接收账号类型: 0-个人(对私) 1-商户(对公)")
    private Byte accType;

    /**
     * 分账接收账号
     */
    @Schema(title = "accNo", description = "分账接收账号")
    private String accNo;

    /**
     * 分账接收账号名称
     */
    @Schema(title = "accName", description = "分账接收账号名称")
    private String accName;

    /**
     * 分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等
     */
    @Schema(title = "relationType", description = "分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等")
    private String relationType;

    /**
     * 当选择自定义时，需要录入该字段。 否则为对应的名称
     */
    @Schema(title = "relationTypeName", description = "当选择自定义时，需要录入该字段。 否则为对应的名称")
    private String relationTypeName;

    /**
     * 分账比例
     */
    @Schema(title = "divisionProfit", description = "分账比例")
    private BigDecimal divisionProfit;

    /**
     * 分账状态（本系统状态，并不调用上游关联关系）: 1-正常分账, 0-暂停分账
     */
    @Schema(title = "state", description = "分账状态（本系统状态，并不调用上游关联关系）: 1-正常分账, 0-暂停分账")
    private Byte state;

    /**
     * 上游绑定返回信息，一般用作查询绑定异常时的记录
     */
    @Schema(title = "channelBindResult", description = "上游绑定返回信息，一般用作查询绑定异常时的记录")
    private String channelBindResult;

    /**
     * 渠道特殊信息
     */
    @Schema(title = "channelExtInfo", description = "渠道特殊信息")
    private String channelExtInfo;

    /**
     * 绑定成功时间
     */
    @Schema(title = "bindSuccessTime", description = "绑定成功时间")
    private Date bindSuccessTime;

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
