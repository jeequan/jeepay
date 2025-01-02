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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 服务商信息表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Schema(description = "服务商信息表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_isv_info")
public class IsvInfo extends BaseModel implements Serializable {

    //gw
    public static final LambdaQueryWrapper<IsvInfo> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    /**
     * 服务商号
     */
    @Schema(title = "isvNo", description = "服务商号")
    @TableId(value = "isv_no", type = IdType.INPUT)
    private String isvNo;

    /**
     * 服务商名称
     */
    @Schema(title = "isvName", description = "服务商名称")
    private String isvName;

    /**
     * 服务商简称
     */
    @Schema(title = "isvShortName", description = "服务商简称")
    private String isvShortName;

    /**
     * 联系人姓名
     */
    @Schema(title = "contactName", description = "联系人姓名")
    private String contactName;

    /**
     * 联系人手机号
     */
    @Schema(title = "contactTel", description = "联系人手机号")
    private String contactTel;

    /**
     * 联系人邮箱
     */
    @Schema(title = "contactEmail", description = "联系人邮箱")
    private String contactEmail;

    /**
     * 状态: 0-停用, 1-正常
     */
    @Schema(title = "state", description = "状态: 0-停用, 1-正常")
    private Byte state;

    /**
     * 备注
     */
    @Schema(title = "remark", description = "备注")
    private String remark;

    /**
     * 创建者用户ID
     */
    @Schema(title = "createdUid", description = "创建者用户ID")
    private Long createdUid;

    /**
     * 创建者姓名
     */
    @Schema(title = "createdBy", description = "创建者姓名")
    private String createdBy;

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
