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

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.CleanMchLoginAuthCacheMQ;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.MchInfoService;
import com.jeequan.jeepay.service.impl.SysUserAuthService;
import com.jeequan.jeepay.service.impl.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 商户管理类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@Tag(name = "商户基本信息管理")
@RestController
@RequestMapping("/api/mchInfo")
public class MchInfoController extends CommonCtrl {

    @Autowired private MchInfoService mchInfoService;
    @Autowired private SysUserService sysUserService;
    @Autowired private SysUserAuthService sysUserAuthService;
    @Autowired private IMQSender mqSender;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 商户信息列表
     */
    @Operation(summary = "查询商户列表", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "mchNo", description = "商户号", required = true),
            @Parameter(name = "mchName", description = "商户名称"),
            @Parameter(name = "isvNo", description = "服务商号"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用"),
            @Parameter(name = "type", description = "类型: 1-普通商户, 2-特约商户(服务商模式)")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<MchInfo> list() {
        MchInfo mchInfo = getObject(MchInfo.class);

        LambdaQueryWrapper<MchInfo> wrapper = MchInfo.gw();
        if (StringUtils.isNotEmpty(mchInfo.getMchNo())) {
            wrapper.eq(MchInfo::getMchNo, mchInfo.getMchNo());
        }
        if (StringUtils.isNotEmpty(mchInfo.getIsvNo())) {
            wrapper.eq(MchInfo::getIsvNo, mchInfo.getIsvNo());
        }
        if (StringUtils.isNotEmpty(mchInfo.getMchName())) {
            wrapper.eq(MchInfo::getMchName, mchInfo.getMchName());
        }
        if (mchInfo.getType() != null) {
            wrapper.eq(MchInfo::getType, mchInfo.getType());
        }
        if (mchInfo.getState() != null) {
            wrapper.eq(MchInfo::getState, mchInfo.getState());
        }
        wrapper.orderByDesc(MchInfo::getCreatedAt);

        IPage<MchInfo> pages = mchInfoService.page(getIPage(), wrapper);
        return ApiPageRes.pages(pages);
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 新增商户信息
     */
    @Operation(summary = "新增商户信息", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchName", description = "商户名称", required = true),
            @Parameter(name = "mchShortName", description = "商户简称", required = true),
            @Parameter(name = "loginUserName", description = "登录名", required = true),
            @Parameter(name = "isvNo", description = "服务商号，type为2时必填"),
            @Parameter(name = "contactName", description = "联系人姓名", required = true),
            @Parameter(name = "contactTel", description = "联系人手机号", required = true),
            @Parameter(name = "contactEmail", description = "联系人邮箱"),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用"),
            @Parameter(name = "type", description = "类型: 1-普通商户, 2-特约商户(服务商模式)")
    })
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
    @Operation(summary = "删除商户信息", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchNo", description = "商户号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_DEL')")
    @MethodLog(remark = "删除商户")
    @RequestMapping(value="/{mchNo}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("mchNo") String mchNo) {
        List<Long> userIdList = mchInfoService.removeByMchNo(mchNo);

        // 推送mq删除redis用户缓存
        mqSender.send(CleanMchLoginAuthCacheMQ.build(userIdList));

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_INFO, null, mchNo, null));
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 更新商户信息
     */
    @Operation(summary = "更新商户信息", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchNo", description = "商户号", required = true),
            @Parameter(name = "mchName", description = "商户名称", required = true),
            @Parameter(name = "mchShortName", description = "商户简称", required = true),
            @Parameter(name = "loginUserName", description = "登录名", required = true),
            @Parameter(name = "contactName", description = "联系人姓名", required = true),
            @Parameter(name = "contactTel", description = "联系人手机号", required = true),
            @Parameter(name = "contactEmail", description = "联系人邮箱"),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用"),
            @Parameter(name = "resetPass", description = "是否重置密码"),
            @Parameter(name = "confirmPwd", description = "待更新的密码，base64加密"),
            @Parameter(name = "defaultPass", description = "是否默认密码")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @MethodLog(remark = "更新商户信息")
    @RequestMapping(value="/{mchNo}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("mchNo") String mchNo) {

        //获取查询条件
        MchInfo mchInfo = getObject(MchInfo.class);
        mchInfo.setMchNo(mchNo); //设置商户号主键

        mchInfo.setType(null); //防止变更商户类型
        mchInfo.setIsvNo(null);

        // 待删除用户登录信息的ID list
        Set<Long> removeCacheUserIdList = new HashSet<>();

        // 如果商户状态为禁用状态，清除该商户用户登录信息
        if (mchInfo.getState() == CS.NO) {
            sysUserService.list( SysUser.gw().select(SysUser::getSysUserId).eq(SysUser::getBelongInfoId, mchNo).eq(SysUser::getSysType, CS.SYS_TYPE.MCH) )
            .stream().forEach(u -> removeCacheUserIdList.add(u.getSysUserId()));
        }

        //判断是否重置密码
        if (getReqParamJSON().getBooleanValue("resetPass")) {
            // 待更新的密码
            String updatePwd = getReqParamJSON().getBoolean("defaultPass") ? CS.DEFAULT_PWD : Base64.decodeStr(getValStringRequired("confirmPwd")) ;
            // 获取商户超管
            Long mchAdminUserId = sysUserService.findMchAdminUserId(mchNo);

            //重置超管密码
            sysUserAuthService.resetAuthInfo(mchAdminUserId, null, null, updatePwd, CS.SYS_TYPE.MCH);

            //删除超管登录信息
            removeCacheUserIdList.add(mchAdminUserId);
        }

        // 推送mq删除redis用户认证信息
        if (!removeCacheUserIdList.isEmpty()) {
            mqSender.send(CleanMchLoginAuthCacheMQ.build(new ArrayList<>(removeCacheUserIdList)));
        }

        //更新商户信息
        if (!mchInfoService.updateById(mchInfo)) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_INFO, null, mchNo, null));

        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:14
     * @describe: 查询商户信息
     */
    @Operation(summary = "查询商户信息", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchNo", description = "商户号", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_MCH_INFO_VIEW', 'ENT_MCH_INFO_EDIT')")
    @RequestMapping(value="/{mchNo}", method = RequestMethod.GET)
    public ApiRes<MchInfo> detail(@PathVariable("mchNo") String mchNo) {
        MchInfo mchInfo = mchInfoService.getById(mchNo);
        if (mchInfo == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        SysUser sysUser = sysUserService.getById(mchInfo.getInitUserId());
        if (sysUser != null) {
            mchInfo.addExt("loginUserName", sysUser.getLoginUsername());
        }
        return ApiRes.ok(mchInfo);
    }
}
