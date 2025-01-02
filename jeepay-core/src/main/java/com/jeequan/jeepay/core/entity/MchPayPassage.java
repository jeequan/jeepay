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
 * 商户支付通道表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Schema(description = "商户支付通道表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_mch_pay_passage")
public class MchPayPassage extends BaseModel implements Serializable {

    public static final LambdaQueryWrapper<MchPayPassage> gw(){
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
     * 商户号
     */
    @Schema(title = "mchNo", description = "商户号")
    private String mchNo;

    /**
     * 应用ID
     */
    @Schema(title = "appId", description = "应用ID")
    private String appId;

    /**
     * 支付接口
     */
    @Schema(title = "ifCode", description = "支付接口")
    private String ifCode;

    /**
     * 支付方式
     */
    @Schema(title = "wayCode", description = "支付方式")
    private String wayCode;

    /**
     * 支付方式费率
     */
    @Schema(title = "rate", description = "支付方式费率")
    private BigDecimal rate;

    /**
     * 风控数据
     */
    @Schema(title = "riskConfig", description = "风控数据")
    private String riskConfig;

    /**
     * 状态: 0-停用, 1-启用
     */
    @Schema(title = "state", description = "状态: 0-停用, 1-启用")
    private Byte state;

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
