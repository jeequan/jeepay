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
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.entity.SysUserAuth;
import com.jeequan.jeepay.core.entity.SysUserRoleRela;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.service.mapper.SysUserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 系统操作员表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2020-06-13
 */
@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    @Autowired private SysUserAuthService sysUserAuthService;
    @Autowired private SysUserRoleRelaService sysUserRoleRelaService;


    /** 添加系统用户 **/
    @Transactional
    public void addSysUser(SysUser sysUser, String sysType){

        //判断获取到选择的角色集合
//        String roleIdListStr = sysUser.extv().getString("roleIdListStr");
//        if(StringKit.isEmpty(roleIdListStr)) throw new BizException("请选择角色信息！");
//
//        List<String> roleIdList = JSONArray.parseArray(roleIdListStr, String.class);
//        if(roleIdList.isEmpty()) throw new BizException("请选择角色信息！");

        // 判断数据来源
        if( StringUtils.isEmpty(sysUser.getLoginUsername()) ) {
            throw new BizException("登录用户名不能为空！");
        }
        if( StringUtils.isEmpty(sysUser.getRealname()) ) {
            throw new BizException("姓名不能为空！");
        }
        if( StringUtils.isEmpty(sysUser.getTelphone()) ) {
            throw new BizException("手机号不能为空！");
        }
        if(sysUser.getSex() == null ) {
            throw new BizException("性别不能为空！");
        }

        //登录用户名不可重复
        if( count(SysUser.gw().eq(SysUser::getSysType, sysType).eq(SysUser::getLoginUsername, sysUser.getLoginUsername())) > 0 ){
            throw new BizException("登录用户名已存在！");
        }
        //手机号不可重复
        if( count(SysUser.gw().eq(SysUser::getSysType, sysType).eq(SysUser::getTelphone, sysUser.getTelphone())) > 0 ){
            throw new BizException("手机号已存在！");
        }
        //员工号不可重复
        if( count(SysUser.gw().eq(SysUser::getSysType, sysType).eq(SysUser::getUserNo, sysUser.getUserNo())) > 0 ){
            throw new BizException("员工号已存在！");
        }

        //女  默认头像
        if(sysUser.getSex() != null && CS.SEX_FEMALE == sysUser.getSex()){
            sysUser.setAvatarUrl("https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/defava_f.png");
        }else{
            sysUser.setAvatarUrl("https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/defava_m.png");
        }

        //1. 插入用户主表
        sysUser.setSysType(sysType); // 系统类型
        this.save(sysUser);

        Long sysUserId = sysUser.getSysUserId();

        //添加到 user_auth表
        String authPwd = CS.DEFAULT_PWD;

        sysUserAuthService.addUserAuthDefault(sysUserId, sysUser.getLoginUsername(), sysUser.getTelphone(), authPwd, sysType);

        //3. 添加用户角色信息
        //saveUserRole(sysUser.getSysUserId(), new ArrayList<>());

    }

    //修改用户信息
    @Transactional
    public void updateSysUser(SysUser sysUser){

        Long sysUserId = sysUser.getSysUserId();
        SysUser dbRecord = getById(sysUserId);

        if (dbRecord == null) {
            throw new BizException("该用户不存在");
        }

        //修改了手机号， 需要修改auth表信息
        if(!dbRecord.getTelphone().equals(sysUser.getTelphone())){

            if(count(SysUser.gw().eq(SysUser::getSysType, dbRecord.getSysType()).eq(SysUser::getTelphone, sysUser.getTelphone())) > 0){
                throw new BizException("该手机号已关联其他用户！");
            }

            sysUserAuthService.resetAuthInfo(sysUserId, null, sysUser.getTelphone(), null, dbRecord.getSysType());
        }

        //修改了手机号， 需要修改auth表信息
        if(!dbRecord.getLoginUsername().equals(sysUser.getLoginUsername())){

            if(count(SysUser.gw().eq(SysUser::getSysType, dbRecord.getSysType()).eq(SysUser::getLoginUsername, sysUser.getLoginUsername())) > 0){
                throw new BizException("该登录用户名已关联其他用户！");
            }

            sysUserAuthService.resetAuthInfo(sysUserId, sysUser.getLoginUsername(), null, null, dbRecord.getSysType());
        }

        //修改用户主表
        baseMapper.updateById(sysUser);
    }


    /** 分配用户角色 **/
    @Transactional
    public void saveUserRole(Long userId, List<String> roleIdList) {

        //删除用户之前的 角色信息
        sysUserRoleRelaService.remove(SysUserRoleRela.gw().eq(SysUserRoleRela::getUserId, userId));
        for (String roleId : roleIdList) {
            SysUserRoleRela addRecord = new SysUserRoleRela();
            addRecord.setUserId(userId); addRecord.setRoleId(roleId);
            sysUserRoleRelaService.save(addRecord);
        }
    }

    /** 删除用户 **/
    @Transactional
    public void removeUser(SysUser sysUser, String sysType) {
        // 1.删除用户登录信息
        sysUserAuthService.remove(SysUserAuth.gw()
                .eq(SysUserAuth::getSysType, sysType)
                .in(SysUserAuth::getUserId, sysUser.getSysUserId())
        );
        // 2.删除用户角色信息
        sysUserRoleRelaService.removeById(sysUser.getSysUserId());
        // 3.删除用户信息
        removeById(sysUser.getSysUserId());
    }


    /** 获取到商户的超管用户ID  **/
    public Long findMchAdminUserId(String mchNo){

        return getOne(SysUser.gw().select(SysUser::getSysUserId)
                .eq(SysUser::getBelongInfoId, mchNo)
                .eq(SysUser::getSysType, CS.SYS_TYPE.MCH)
                .eq(SysUser::getIsAdmin, CS.YES)).getSysUserId();

    }

}
