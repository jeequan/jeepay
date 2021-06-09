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
package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.SysRole;
import com.jeequan.jeepay.core.entity.SysRoleEntRela;
import com.jeequan.jeepay.core.entity.SysUserRoleRela;
import com.jeequan.jeepay.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jeequan.jeepay.service.mapper.SysRoleMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 系统角色表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2020-06-13
 */
@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    @Autowired private SysUserRoleRelaService sysUserRoleRelaService;

    @Autowired private SysRoleEntRelaService sysRoleEntRelaService;


    /** 根据用户查询全部角色集合 **/
    public List<String> findListByUser(Long sysUserId){
        List<String> result = new ArrayList<>();
        sysUserRoleRelaService.list(
                SysUserRoleRela.gw().eq(SysUserRoleRela::getUserId, sysUserId)
        ).stream().forEach(r -> result.add(r.getRoleId()));

        return result;
    }


    @Transactional
    public void removeRole(String roleId){

        if(sysUserRoleRelaService.count(SysUserRoleRela.gw().eq(SysUserRoleRela::getRoleId, roleId)) > 0){
            throw new BizException("当前角色已分配到用户， 不可删除！");
        }

        //删除当前表
        removeById(roleId);

        //删除关联表
        sysRoleEntRelaService.remove(SysRoleEntRela.gw().eq(SysRoleEntRela::getRoleId, roleId));

    }



}
