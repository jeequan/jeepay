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
import com.jeequan.jeepay.core.entity.SysUserAuth;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.service.mapper.SysUserAuthMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 操作员认证表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2020-06-13
 */
@Service
public class SysUserAuthService extends ServiceImpl<SysUserAuthMapper, SysUserAuth> {

    /** 根据登录信息查询用户认证信息  **/
    public SysUserAuth selectByLogin(String identifier, Byte identityType, String sysType){
        return baseMapper.selectByLogin(identifier, identityType, sysType);
    }

    /** 添加用户认证表 **/
    @Transactional
    public void addUserAuthDefault(Long userId, String loginUserName, String telPhone, String pwdRaw, String sysType){

        String salt = StringKit.getUUID(6); //6位随机数
        String userPwd = new BCryptPasswordEncoder().encode(pwdRaw);

        /** 用户名登录方式 */
        SysUserAuth record = new SysUserAuth(); record.setUserId(userId); record.setCredential(userPwd); record.setSalt(salt);record.setSysType(sysType);
        record.setIdentityType(CS.AUTH_TYPE.LOGIN_USER_NAME);
        record.setIdentifier(loginUserName);
        save(record);

        /** 手机号登录方式 */
        record = new SysUserAuth(); record.setUserId(userId); record.setCredential(userPwd); record.setSalt(salt);record.setSysType(sysType);
        record.setIdentityType(CS.AUTH_TYPE.TELPHONE);
        record.setIdentifier(telPhone);
        save(record);
    }


    /** 重置密码 */
    @Transactional
    public void resetAuthInfo(Long resetUserId, String authLoginUserName, String telphone, String newPwd, String sysType){

        //更改登录用户名
//        if(StringKit.isNotEmpty(authLoginUserName)){
//            SysUserAuth updateRecord = new SysUserAuth();
//            updateRecord.setIdentifier(authLoginUserName);
//            update(updateRecord, SysUserAuth.gw().eq(SysUserAuth::getSystem, system).eq(SysUserAuth::getUserId, resetUserId).eq(SysUserAuth::getIdentityType, CS.AUTH_TYPE.LOGIN_USER_NAME));
//        }

        //更新手机号认证
//        if(StringKit.isNotEmpty(telphone)){
//            SysUserAuth updateRecord = new SysUserAuth();
//            updateRecord.setIdentifier(telphone);
//            update(updateRecord, SysUserAuth.gw().eq(SysUserAuth::getSystem, system).eq(SysUserAuth::getUserId, resetUserId).eq(SysUserAuth::getIdentityType, CS.AUTH_TYPE.TELPHONE));
//        }

        //更改密码
        if(StringUtils.isNotEmpty(newPwd)){
            //根据当前用户ID 查询出用户的所有认证记录
            List<SysUserAuth> authList = list(SysUserAuth.gw().eq(SysUserAuth::getSysType, sysType).eq(SysUserAuth::getUserId, resetUserId));
            for (SysUserAuth auth : authList) {
                if(StringUtils.isEmpty(auth.getSalt())){ //可能为其他登录方式， 不存在salt
                    continue;
                }
                SysUserAuth updateRecord = new SysUserAuth();
                updateRecord.setAuthId(auth.getAuthId());
                updateRecord.setCredential(new BCryptPasswordEncoder().encode(newPwd));
                updateById(updateRecord);
            }
        }
    }


    /** 查询当前用户密码是否正确 */
    public boolean validateCurrentUserPwd(String pwdRaw){

        //根据当前用户ID + 认证方式为 登录用户名的方式 查询一条记录
        SysUserAuth auth = getOne(SysUserAuth.gw()
                .eq(SysUserAuth::getUserId, JeeUserDetails.getCurrentUserDetails().getSysUser().getSysUserId())
                .eq(SysUserAuth::getIdentityType, CS.AUTH_TYPE.LOGIN_USER_NAME)
        );
        if(auth != null && new BCryptPasswordEncoder().matches(pwdRaw, auth.getCredential())){
            return true;
        }

        return false;
    }


}
