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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 系统角色表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-23
 */
@Schema(description = "系统角色表")
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_role")
public class SysRole implements Serializable {

    //gw
    public static final LambdaQueryWrapper<SysRole> gw(){
        return new LambdaQueryWrapper<>();
    }

    private static final long serialVersionUID=1L;

    /**
     * 角色ID, ROLE_开头
     */
    @Schema(title = "roleId", description = "角色ID, ROLE_开头")
    @TableId
    private String roleId;

    /**
     * 角色名称
     */
    @Schema(title = "roleName", description = "角色名称")
    private String roleName;

    /**
     * 所属系统： MGR-运营平台, MCH-商户中心
     */
    @Schema(title = "sysType", description = "所属系统： MGR-运营平台, MCH-商户中心")
    private String sysType;

    /**
     * 所属商户ID / 0(平台)
     */
    @Schema(title = "belongInfoId", description = "所属商户ID / 0(平台)")
    private String belongInfoId;

    /**
     * 更新时间
     */
    @Schema(title = "updatedAt", description = "更新时间")
    private Date updatedAt;


}
