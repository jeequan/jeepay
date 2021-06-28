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

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.mgr.mq.service.MqServiceImpl;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 商户应用管理类
 *
 * @author zhuxiao
 * @site https://www.jeepay.vip
 * @date 2021-06-16 09:15
 */
@RestController
@RequestMapping("/api/mchApps")
public class MchAppController extends CommonCtrl {

    @Autowired private MchInfoService mchInfoService;
    @Autowired private MchAppService mchAppService;
    @Autowired private MqServiceImpl mqServiceImpl;

    /**
     * @Author: ZhuXiao
     * @Description: 应用列表
     * @Date: 9:59 2021/6/16
    */
    @PreAuthorize("hasAuthority('ENT_MCH_APP_LIST')")
    @GetMapping
    public ApiRes list() {
        MchApp mchApp = getObject(MchApp.class);

        LambdaQueryWrapper<MchApp> wrapper = MchApp.gw();
        if (StringUtils.isNotEmpty(mchApp.getMchNo())) wrapper.eq(MchApp::getMchNo, mchApp.getMchNo());
        if (StringUtils.isNotEmpty(mchApp.getAppId())) wrapper.eq(MchApp::getAppId, mchApp.getAppId());
        if (StringUtils.isNotEmpty(mchApp.getAppName())) wrapper.eq(MchApp::getAppName, mchApp.getAppName());
        if (mchApp.getState() != null) wrapper.eq(MchApp::getState, mchApp.getState());
        wrapper.orderByDesc(MchApp::getCreatedAt);

        IPage<MchApp> pages = mchAppService.page(getIPage(), wrapper);
        return ApiRes.ok(pages);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 新建应用
     * @Date: 10:05 2021/6/16
    */
    @PreAuthorize("hasAuthority('ENT_MCH_APP_ADD')")
    @MethodLog(remark = "新建应用")
    @PostMapping
    public ApiRes add() {
        MchApp mchApp = getObject(MchApp.class);
        mchApp.setAppId(IdUtil.objectId());

        if(mchInfoService.getById(mchApp.getMchNo()) == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        boolean result = mchAppService.save(mchApp);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.ok();
    }

    /**
     * @Author: ZhuXiao
     * @Description: 应用详情
     * @Date: 10:13 2021/6/16
     */
    @PreAuthorize("hasAnyAuthority('ENT_MCH_APP_VIEW', 'ENT_MCH_APP_EDIT')")
    @GetMapping("/{appId}")
    public ApiRes detail(@PathVariable("appId") String appId) {
        MchApp mchApp = mchAppService.getById(appId);
        if (mchApp == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

        return ApiRes.ok(mchApp);
    }

    /**
     * @Author: ZhuXiao
     * @Description: 更新应用信息
     * @Date: 10:11 2021/6/16
    */
    @PreAuthorize("hasAuthority('ENT_MCH_APP_EDIT')")
    @MethodLog(remark = "更新应用信息")
    @PutMapping("/{appId}")
    public ApiRes update(@PathVariable("appId") String appId) {
        MchApp mchApp = getObject(MchApp.class);
        mchApp.setAppId(appId);
        boolean result = mchAppService.updateById(mchApp);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        // 推送修改应用消息
        mqServiceImpl.sendModifyMchApp(mchApp.getMchNo(), mchApp.getAppId());
        return ApiRes.ok();
    }

    /**
     * @Author: ZhuXiao
     * @Description: 删除应用
     * @Date: 10:14 2021/6/16
     */
    @PreAuthorize("hasAuthority('ENT_MCH_APP_DEL')")
    @MethodLog(remark = "删除应用")
    @DeleteMapping("/{appId}")
    public ApiRes delete(@PathVariable("appId") String appId) {

        MchApp mchApp = mchAppService.getById(appId);
        mchAppService.removeByAppId(appId);

        // 推送mq到目前节点进行更新数据
        mqServiceImpl.sendModifyMchApp(mchApp.getMchNo(), appId);
        return ApiRes.ok();
    }

}
