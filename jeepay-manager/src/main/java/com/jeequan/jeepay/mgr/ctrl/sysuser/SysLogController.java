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
package com.jeequan.jeepay.mgr.ctrl.sysuser;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.SysLog;
import com.jeequan.jeepay.core.model.ApiPageRes;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.SysLogService;
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

import java.util.LinkedList;
import java.util.List;

/**
 * 系统日志记录类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@Tag(name = "系统管理（系统日志）")
@RestController
@RequestMapping("api/sysLog")
public class SysLogController extends CommonCtrl {

    @Autowired SysLogService sysLogService;


    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:15
     * @describe: 日志记录列表
     */
    @Operation(summary = "系统日志列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "createdStart", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @Parameter(name = "createdEnd", description = "日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @Parameter(name = "userId", description = "系统用户ID"),
            @Parameter(name = "userName", description = "用户姓名"),
            @Parameter(name = "sysType", description = "所属系统： MGR-运营平台, MCH-商户中心")
    })
    @PreAuthorize("hasAuthority('ENT_LOG_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<SysLog> list() {
        SysLog sysLog = getObject(SysLog.class);
        JSONObject paramJSON = getReqParamJSON();
        // 查询列表
        LambdaQueryWrapper<SysLog> condition = SysLog.gw();
        condition.orderByDesc(SysLog::getCreatedAt);
        if (sysLog.getUserId() != null) {
            condition.eq(SysLog::getUserId, sysLog.getUserId());
        }
        if (sysLog.getUserName() != null) {
            condition.eq(SysLog::getUserName, sysLog.getUserName());
        }
        if (StringUtils.isNotEmpty(sysLog.getSysType())) {
            condition.eq(SysLog::getSysType, sysLog.getSysType());
        }
        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                condition.ge(SysLog::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                condition.le(SysLog::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        IPage<SysLog> pages = sysLogService.page(getIPage(), condition);
        return ApiPageRes.pages(pages);
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:16
     * @describe: 查看日志信息
     */
    @Operation(summary = "系统日志详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "sysLogId", description = "系统日志ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_SYS_LOG_VIEW')")
    @RequestMapping(value="/{sysLogId}", method = RequestMethod.GET)
    public ApiRes<SysLog> detail(@PathVariable("sysLogId") String sysLogId) {
        SysLog sysLog = sysLogService.getById(sysLogId);
        if (sysLog == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(sysLog);
    }

    /**
     * @author: pangxiaoyu
     * @date: 2021/6/7 16:16
     * @describe: 删除日志信息
     */
    @Operation(summary = "删除日志信息")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "selectedIds", description = "系统日志ID（若干个ID用英文逗号拼接）", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_SYS_LOG_DEL')")
    @MethodLog(remark = "删除日志信息")
    @RequestMapping(value="/{selectedIds}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("selectedIds") String selectedIds) {
        String[] ids = selectedIds.split(",");
        List<Long> idsList = new LinkedList<>();
        for (String id : ids) {
            idsList.add(Long.valueOf(id));
        }
        boolean result = sysLogService.removeByIds(idsList);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
        return ApiRes.ok();
    }
}
