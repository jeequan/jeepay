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
package com.jeequan.jeepay.mgr.ctrl.isv;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.IsvInfo;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.IsvInfoService;
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

/**
 * 服务商管理类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@Tag(name = "服务商管理（基本信息）")
@RestController
@RequestMapping("/api/isvInfo")
public class IsvInfoController extends CommonCtrl {

    @Autowired private IsvInfoService isvInfoService;
    @Autowired private IMQSender mqSender;

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:12
     * @describe: 查询服务商信息列表
     */
    @Operation(summary = "服务商列表", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数（-1时查全部数据）"),
            @Parameter(name = "isvNo", description = "服务商编号"),
            @Parameter(name = "isvName", description = "服务商名称"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-正常")
    })
    @PreAuthorize("hasAuthority('ENT_ISV_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<IsvInfo> list() {
        IsvInfo isvInfo = getObject(IsvInfo.class);
        LambdaQueryWrapper<IsvInfo> wrapper = IsvInfo.gw();
        if (StringUtils.isNotEmpty(isvInfo.getIsvNo())) {
            wrapper.eq(IsvInfo::getIsvNo, isvInfo.getIsvNo());
        }
        if (StringUtils.isNotEmpty(isvInfo.getIsvName())) {
            wrapper.eq(IsvInfo::getIsvName, isvInfo.getIsvName());
        }
        if (isvInfo.getState() != null) {
            wrapper.eq(IsvInfo::getState, isvInfo.getState());
        }
        wrapper.orderByDesc(IsvInfo::getCreatedAt);
        IPage<IsvInfo> pages = isvInfoService.page(getIPage(true), wrapper);

        return ApiPageRes.pages(pages);
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:13
     * @describe: 新增服务商信息
     */
    @Operation(summary = "新增服务商", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "isvName", description = "服务商名称", required = true),
            @Parameter(name = "contactName", description = "联系人姓名", required = true),
            @Parameter(name = "contactTel", description = "联系人手机号"),
            @Parameter(name = "contactEmail", description = "联系人邮箱"),
            @Parameter(name = "isvShortName", description = "服务商简称"),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-正常")
    })
    @PreAuthorize("hasAuthority('ENT_ISV_INFO_ADD')")
    @MethodLog(remark = "新增服务商")
    @RequestMapping(value="", method = RequestMethod.POST)
    public ApiRes add() {
        IsvInfo isvInfo = getObject(IsvInfo.class);
        String isvNo = "V" + DateUtil.currentSeconds();
        isvInfo.setIsvNo(isvNo);
        isvInfo.setCreatedUid(getCurrentUser().getSysUser().getSysUserId());
        isvInfo.setCreatedBy(getCurrentUser().getSysUser().getRealname());
        boolean result = isvInfoService.save(isvInfo);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:13
     * @describe: 删除服务商信息
     */
    @Operation(summary = "删除服务商", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "isvNo", description = "服务商号", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_ISV_INFO_DEL')")
    @MethodLog(remark = "删除服务商")
    @RequestMapping(value="/{isvNo}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("isvNo") String isvNo) {
        isvInfoService.removeByIsvNo(isvNo);

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_ISV_INFO, isvNo, null, null));
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:13
     * @describe: 更新服务商信息
     */
    @Operation(summary = "更新服务商信息", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "isvNo", description = "服务商号", required = true),
            @Parameter(name = "isvName", description = "服务商名称", required = true),
            @Parameter(name = "contactName", description = "联系人姓名", required = true),
            @Parameter(name = "contactTel", description = "联系人手机号"),
            @Parameter(name = "contactEmail", description = "联系人邮箱"),
            @Parameter(name = "isvShortName", description = "服务商简称"),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-正常")
    })
    @PreAuthorize("hasAuthority('ENT_ISV_INFO_EDIT')")
    @MethodLog(remark = "更新服务商信息")
    @RequestMapping(value="/{isvNo}", method = RequestMethod.PUT)
    public ApiRes update(@PathVariable("isvNo") String isvNo) {
        IsvInfo isvInfo = getObject(IsvInfo.class);
        isvInfo.setIsvNo(isvNo);
        boolean result = isvInfoService.updateById(isvInfo);

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_ISV_INFO, isvNo, null, null));

        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:13
     * @describe: 查看服务商信息
     */
    @Operation(summary = "查看服务商信息", description = "")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name= "isvNo", description = "服务商编号", required = true)
    })
    @PreAuthorize("hasAnyAuthority('ENT_ISV_INFO_VIEW', 'ENT_ISV_INFO_EDIT')")
    @RequestMapping(value="/{isvNo}", method = RequestMethod.GET)
    public ApiRes<IsvInfo> detail(@PathVariable("isvNo") String isvNo) {
        IsvInfo isvInfo = isvInfoService.getById(isvNo);
        if (isvInfo == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(isvInfo);
    }
}
