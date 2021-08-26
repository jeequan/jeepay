package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 分账账号组
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-08-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_division_receiver_group")
public class MchDivisionReceiverGroup implements Serializable {

    //gw
    public static final LambdaQueryWrapper<MchDivisionReceiverGroup> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    /**
     * 组ID
     */
    @TableId(value = "receiver_group_id", type = IdType.AUTO)
    private Long receiverGroupId;

    /**
     * 组名称
     */
    private String receiverGroupName;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 自动分账组（当订单分账模式为自动分账，改组将完成分账逻辑） 0-否 1-是
     */
    private Byte autoDivisionFlag;

    /**
     * 创建者用户ID
     */
    private Long createdUid;

    /**
     * 创建者姓名
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;



}
