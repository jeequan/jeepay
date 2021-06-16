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
package com.jeequan.jeepay.mgr.ctrl.merchant;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.mgr.mq.queue.MqQueue4ModifyMchUserRemove;
import com.jeequan.jeepay.mgr.mq.topic.MqTopic4ModifyMchInfo;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import com.jeequan.jeepay.service.impl.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 商户管理类
 *
 * @author pangxiaoyu
 * @site https://www.jeepay.vip
 * @date 2021-06-07 07:15
 */
@RestController
@RequestMapping("/api/mchInfo")
public class MchInfoController extends CommonCtrl {

    @Autowired private MchInfoService mchInfoService;
    @Autowired private SysUserService sysUserService;
    @Autowired private MqTopic4ModifyMchInfo mqTopic4ModifyMchInfo;
    @Autowired private MqQueue4ModifyMchUserRemove mqQueue4ModifyMchUserRemove;
    @Autowired private SysUserAuthService sysUserAuthService;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 商户信息列表
     */
    @PreAuthorize("hasAuthority('ENT_MCH_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiRes list() {
        MchInfo mchInfo = getObject(MchInfo.class);

        LambdaQueryWrapper<MchInfo> wrapper = MchInfo.gw();
        if (StringUtils.isNotEmpty(mchInfo.getMchNo())) wrapper.eq(MchInfo::getMchNo, mchInfo.getMchNo());
        if (StringUtils.isNotEmpty(mchInfo.getIsvNo())) wrapper.eq(MchInfo::getIsvNo, mchInfo.getIsvNo());
        if (StringUtils.isNotEmpty(mchInfo.getMchName())) wrapper.eq(MchInfo::getMchName, mchInfo.getMchName());
        if (mchInfo.getType() != null) wrapper.eq(MchInfo::getType, mchInfo.getType());
        if (mchInfo.getState() != null) wrapper.eq(MchInfo::getState, mchInfo.getState());
        wrapper.orderByDesc(MchInfo::getCreatedAt);

        IPage<MchInfo> pages = mchInfoService.page(getIPage(), wrapper);
        return ApiRes.page(pages);
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 新增商户信息
     */
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_ADD')")
    @MethodLog(remark = "新增商户")
    @RequestMapping(value="", method = RequestMethod.POST)
    public ApiRes add() {
        MchInfo mchInfo = getObject(MchInfo.class);
        // 获取传入的商户登录名
        String loginUserName = getValStringRequired("loginUserName");
        mchInfo.setMchNo("M" + DateUtil.currentSeconds());
        // 当前登录用户信息
        SysUser sysUser = getCurrentUser().getSysUser();
        mchInfo.setCreatedUid(sysUser.getSysUserId());
        mchInfo.setCreatedBy(sysUser.getRealname());

        mchInfoService.addMch(mchInfo, loginUserName);
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 删除商户信息
     */
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_DEL')")
    @MethodLog(remark = "删除商户")
    @RequestMapping(value="/{mchNo}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("mchNo") String mchNo) {
        List<Long> userIdList = mchInfoService.removeByMchNo(mchNo);
        // 推送mq删除redis用户缓存
        mqQueue4ModifyMchUserRemove.push(StringUtils.join(userIdList, ","));
        // 推送mq到目前节点进行更新数据
        mqTopic4ModifyMchInfo.push(mchNo);
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 更新商户信息
     */
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @MethodLog(remark = "更新商户信息")
    @RequestMapping(value="/{mchNo}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("mchNo") String mchNo) {
        MchInfo mchInfo = getObject(MchInfo.class);
        mchInfo.setMchNo(mchNo);
        // 校验该商户是否为特邀商户
        MchInfo dbMchInfo = mchInfoService.getById(mchNo);
        if (dbMchInfo == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        // 如果为特邀商户则不允许修改服务商及商户类型
        if (dbMchInfo.getType() == CS.MCH_TYPE_ISVSUB) {
            mchInfo.setType(dbMchInfo.getType());
            mchInfo.setIsvNo(dbMchInfo.getIsvNo());
        }
        // 如果商户状态为禁用状态，清除该商户用户登录信息
        if (mchInfo.getState() == CS.NO) {
            List<Long> userIdList = new ArrayList<>();
            List<SysUser> userList = sysUserService.list(SysUser.gw()
                    .eq(SysUser::getBelongInfoId, mchNo)
                    .eq(SysUser::getSysType, CS.SYS_TYPE.MCH)
            );
            if (userList.size() > 0) {
                for (SysUser user:userList) {
                    userIdList.add(user.getSysUserId());
                }
            }
            // 推送mq删除redis用户缓存
            mqQueue4ModifyMchUserRemove.push(StringUtils.join(userIdList, ","));
        }
        //判断是否重置密码
        Boolean resetPass = getReqParamJSON().getBoolean("resetPass");
        if (resetPass != null && resetPass) {
            Boolean defaultPass = getReqParamJSON().getBoolean("defaultPass");
            String updatePwd = "";
            if (!defaultPass) {
                // 获取修改的密码
                updatePwd = getValStringRequired("confirmPwd");
            }else {
                // 重置默认密码
                updatePwd = CS.DEFAULT_PWD;
            }
            // 获取商户最初的用户
            List<SysUser> userList = sysUserService.list(SysUser.gw()
                    .eq(SysUser::getBelongInfoId, mchNo)
                    .eq(SysUser::getSysType, CS.SYS_TYPE.MCH)
                    .orderByAsc(SysUser::getCreatedAt)
            );
            sysUserAuthService.resetAuthInfo(userList.get(0).getSysUserId(), null, null, updatePwd, CS.SYS_TYPE.MCH);
            // 推送mq删除redis用户缓存
            mqQueue4ModifyMchUserRemove.push(StringUtils.join(Arrays.asList(userList.get(0).getSysUserId()), ","));
        }

        boolean result = mchInfoService.updateById(mchInfo);
        mqTopic4ModifyMchInfo.push(mchNo); // 推送mq到目前节点进行更新数据
        if (!result)  return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 查询商户信息
     */
    @PreAuthorize("hasAnyAuthority('ENT_MCH_INFO_VIEW', 'ENT_MCH_INFO_EDIT')")
    @RequestMapping(value="/{mchNo}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("mchNo") String mchNo) {
        MchInfo mchInfo = mchInfoService.getById(mchNo);
        if (mchInfo == null) return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);

        SysUser sysUser = sysUserService.getById(mchInfo.getInitUserId());
        if (sysUser != null) mchInfo.addExt("loginUserName", sysUser.getLoginUsername());
        return ApiRes.ok(mchInfo);
    }
}
