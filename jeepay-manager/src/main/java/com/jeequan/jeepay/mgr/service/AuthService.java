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
package com.jeequan.jeepay.mgr.service;

import cn.hutool.core.util.IdUtil;
import com.jeequan.jeepay.core.cache.ITokenService;
import com.jeequan.jeepay.core.cache.RedisUtil;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.exception.JeepayAuthenticationException;
import com.jeequan.jeepay.core.jwt.JWTPayload;
import com.jeequan.jeepay.core.jwt.JWTUtils;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.mgr.config.SystemYmlConfig;
import com.jeequan.jeepay.service.impl.SysRoleEntRelaService;
import com.jeequan.jeepay.service.impl.SysRoleService;
import com.jeequan.jeepay.service.impl.SysUserService;
import com.jeequan.jeepay.service.mapper.SysEntitlementMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/*
* 认证Service
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:12
*/
@Slf4j
@Service
public class AuthService {

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired private SysUserService sysUserService;
    @Autowired private SysRoleService sysRoleService;
    @Autowired private SysRoleEntRelaService sysRoleEntRelaService;
    @Autowired private SysEntitlementMapper sysEntitlementMapper;
    @Autowired private SystemYmlConfig systemYmlConfig;

    /**
     * 认证
     * **/
    public String auth(String username, String password){

        //1. 生成spring-security usernamePassword类型对象
        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password);

        //spring-security 自动认证过程；
        // 1. 进入 JeeUserDetailsServiceImpl.loadUserByUsername 获取用户基本信息；
        //2. SS根据UserDetails接口验证是否用户可用；
        //3. 最后返回loadUserByUsername 封装的对象信息；
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(upToken);
        } catch (JeepayAuthenticationException jex) {
            throw jex.getBizException() == null ? new BizException(jex.getMessage()) : jex.getBizException();
        } catch (BadCredentialsException e) {
            throw new BizException("用户名/密码错误！");
        } catch (AuthenticationException e) {
            log.error("AuthenticationException:", e);
            throw new BizException("认证服务出现异常， 请重试或联系系统管理员！");
        }

        JeeUserDetails jeeUserDetails = (JeeUserDetails) authentication.getPrincipal();

        //验证通过后 再查询用户角色和权限信息集合

        SysUser sysUser = jeeUserDetails.getSysUser();

        //非超级管理员 && 不包含左侧菜单 进行错误提示
        if(sysUser.getIsAdmin() != CS.YES && sysEntitlementMapper.userHasLeftMenu(sysUser.getSysUserId(), CS.SYS_TYPE.MGR) <= 0){
            throw new BizException("当前用户未分配任何菜单权限，请联系管理员进行分配后再登录！");
        }

        // 放置权限集合
        jeeUserDetails.setAuthorities(getUserAuthority(sysUser));

        //生成token
        String cacheKey = CS.getCacheKeyToken(sysUser.getSysUserId(), IdUtil.fastUUID());

        //生成iToken 并放置到缓存
        ITokenService.processTokenCache(jeeUserDetails, cacheKey); //处理token 缓存信息

        //将信息放置到Spring-security context中
        UsernamePasswordAuthenticationToken authenticationRest = new UsernamePasswordAuthenticationToken(jeeUserDetails, null, jeeUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationRest);

        //返回JWTToken
        return JWTUtils.generateToken(new JWTPayload(jeeUserDetails), systemYmlConfig.getJwtSecret());
    }

    /** 根据用户ID 更新缓存中的权限集合， 使得分配实时生效  **/
    public void refAuthentication(List<Long> sysUserIdList){

        if(sysUserIdList == null || sysUserIdList.isEmpty()){
            return ;
        }

        Map<Long, SysUser> sysUserMap = new HashMap<>();

        // 查询 sysUserId 和 state
        sysUserService.list(
                SysUser.gw()
                        .select(SysUser::getSysUserId, SysUser::getState)
                        .in(SysUser::getSysUserId, sysUserIdList)
        ).stream().forEach(item -> sysUserMap.put(item.getSysUserId(), item));

        for (Long sysUserId : sysUserIdList) {

            Collection<String> cacheKeyList = RedisUtil.keys(CS.getCacheKeyToken(sysUserId, "*"));
            if(cacheKeyList == null || cacheKeyList.isEmpty()){
                continue;
            }

            for (String cacheKey : cacheKeyList) {

                //用户不存在 || 已禁用 需要删除Redis
                if(sysUserMap.get(sysUserId) == null || sysUserMap.get(sysUserId).getState() == CS.PUB_DISABLE){
                    RedisUtil.del(cacheKey);
                    continue;
                }

                JeeUserDetails jwtBaseUser = RedisUtil.getObject(cacheKey, JeeUserDetails.class);
                if(jwtBaseUser == null){
                    continue;
                }

                // 重新放置sysUser对象
                jwtBaseUser.setSysUser(sysUserService.getById(sysUserId));

                //查询放置权限数据
                jwtBaseUser.setAuthorities(getUserAuthority(jwtBaseUser.getSysUser()));

                //保存token  失效时间不变
                RedisUtil.set(cacheKey, jwtBaseUser);
            }
        }

    }

    /** 根据用户ID 删除用户缓存信息  **/
    public void delAuthentication(List<Long> sysUserIdList){
        if(sysUserIdList == null || sysUserIdList.isEmpty()){
            return ;
        }
        for (Long sysUserId : sysUserIdList) {
            Collection<String> cacheKeyList = RedisUtil.keys(CS.getCacheKeyToken(sysUserId, "*"));
            if(cacheKeyList == null || cacheKeyList.isEmpty()){
                continue;
            }
            for (String cacheKey : cacheKeyList) {
                RedisUtil.del(cacheKey);
            }
        }
    }

    public List<SimpleGrantedAuthority> getUserAuthority(SysUser sysUser){

        //用户拥有的角色集合  需要以ROLE_ 开头,  用户拥有的权限集合
        List<String> roleList = sysRoleService.findListByUser(sysUser.getSysUserId());
        List<String> entList = sysRoleEntRelaService.selectEntIdsByUserId(sysUser.getSysUserId(), sysUser.getIsAdmin(), sysUser.getSysType());

        List<SimpleGrantedAuthority> grantedAuthorities = new LinkedList<>();
        roleList.stream().forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority(role)));
        entList.stream().forEach(ent -> grantedAuthorities.add(new SimpleGrantedAuthority(ent)));
        return grantedAuthorities;
    }


}
