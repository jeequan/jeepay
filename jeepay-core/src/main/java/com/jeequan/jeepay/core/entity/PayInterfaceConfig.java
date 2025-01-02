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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 支付接口配置参数表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Schema(description = "支付接口配置参数表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pay_interface_config")
public class PayInterfaceConfig extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<PayInterfaceConfig> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    @Schema(title = "id", description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账号类型:1-服务商 2-商户
     */
    @Schema(title = "infoType", description = "账号类型:1-服务商 2-商户")
    private Byte infoType;

    /**
     * 服务商或商户No
     */
    @Schema(title = "infoId", description = "服务商号（服务商支付参数）或应用AppId（商户支付参数）")
    private String infoId;

    /**
     * 支付接口代码
     */
    @Schema(title = "ifCode", description = "支付接口代码")
    private String ifCode;

    /**
     * 接口配置参数,json字符串
     */
    @Schema(title = "ifParams", description = "接口配置参数,json字符串")
    private String ifParams;

    /**
     * 支付接口费率
     */
    @Schema(title = "ifRate", description = "支付接口费率")
    private BigDecimal ifRate;

    /**
     * 状态: 0-停用, 1-启用
     */
    @Schema(title = "state", description = "状态: 0-停用, 1-启用")
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
     * 更新者用户ID
     */
    @Schema(title = "updatedUid", description = "更新者用户ID")
    private Long updatedUid;

    /**
     * 更新者姓名
     */
    @Schema(title = "updatedBy", description = "更新者姓名")
    private String updatedBy;

    /**
     * 更新时间
     */
    @Schema(title = "updatedAt", description = "更新时间")
    private Date updatedAt;


}
