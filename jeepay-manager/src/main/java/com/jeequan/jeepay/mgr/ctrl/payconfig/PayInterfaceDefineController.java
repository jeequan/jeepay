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
package com.jeequan.jeepay.mgr.ctrl.payconfig;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PayInterfaceConfig;
import com.jeequan.jeepay.core.entity.PayInterfaceDefine;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayInterfaceConfigService;
import com.jeequan.jeepay.service.impl.PayInterfaceDefineService;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付接口定义管理类
 *
 * @author zhuxiao
 * @site https://www.jeequan.com
 * @date 2021-04-27 15:50
 */
@RestController
@RequestMapping("api/payIfDefines")
public class PayInterfaceDefineController extends CommonCtrl {

    @Autowired private PayInterfaceDefineService payInterfaceDefineService;
    @Autowired private PayOrderService payOrderService;
    @Autowired private PayInterfaceConfigService payInterfaceConfigService;

    /**
     * @Author: ZhuXiao
     * @Description: list
     * @Date: 15:51 2021/4/27
    */
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_LIST')")
    @GetMapping
    public ApiRes list() {

        List<PayInterfaceDefine> list = payInterfaceDefineService.list(PayInterfaceDefine.gw()
                .orderByAsc(PayInterfaceDefine::getCreatedAt)
        );
        return ApiRes.ok(list);
    }


    /**
     * @Author: ZhuXiao
     * @Description: detail
     * @Date: 15:51 2021/4/27
    */
    @PreAuthorize("hasAnyAuthority('ENT_PC_IF_DEFINE_VIEW', 'ENT_PC_IF_DEFINE_EDIT')")
    @GetMapping("/{ifCode}")
    public ApiRes detail(@PathVariable("ifCode") String ifCode) {
        return ApiRes.ok(payInterfaceDefineService.getById(ifCode));
    }

    /**
     * @Author: ZhuXiao
     * @Description: add
     * @Date: 15:51 2021/4/27
    */
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_ADD')")
    @PostMapping
    @MethodLog(remark = "新增支付接口")
    public ApiRes add() {
        PayInterfaceDefine payInterfaceDefine = getObject(PayInterfaceDefine.class);

        JSONArray jsonArray = new JSONArray();
        String[] wayCodes = getValStringRequired("wayCodeStrs").split(",");
        for (String wayCode : wayCodes) {
            JSONObject object = new JSONObject();
            object.put("wayCode", wayCode);
            jsonArray.add(object);
        }
        payInterfaceDefine.setWayCodes(jsonArray);

        boolean result = payInterfaceDefineService.save(payInterfaceDefine);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        return ApiRes.ok();
    }

    /**
     * @Author: ZhuXiao
     * @Description: update
     * @Date: 15:51 2021/4/27
    */
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_EDIT')")
    @PutMapping("/{ifCode}")
    @MethodLog(remark = "更新支付接口")
    public ApiRes update(@PathVariable("ifCode") String ifCode) {
        PayInterfaceDefine payInterfaceDefine = getObject(PayInterfaceDefine.class);
        payInterfaceDefine.setIfCode(ifCode);

        JSONArray jsonArray = new JSONArray();
        String[] wayCodes = getValStringRequired("wayCodeStrs").split(",");
        for (String wayCode : wayCodes) {
            JSONObject object = new JSONObject();
            object.put("wayCode", wayCode);
            jsonArray.add(object);
        }
        payInterfaceDefine.setWayCodes(jsonArray);

        boolean result = payInterfaceDefineService.updateById(payInterfaceDefine);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    /**
     * @Author: ZhuXiao
     * @Description: delete
     * @Date: 15:52 2021/4/27
    */
    @PreAuthorize("hasAuthority('ENT_PC_IF_DEFINE_DEL')")
    @DeleteMapping("/{ifCode}")
    @MethodLog(remark = "删除支付接口")
    public ApiRes delete(@PathVariable("ifCode") String ifCode) {

        // 校验该支付方式是否有服务商或商户配置参数或者已有订单
        if (payInterfaceConfigService.count(PayInterfaceConfig.gw().eq(PayInterfaceConfig::getIfCode, ifCode)) > 0
                || payOrderService.count(PayOrder.gw().eq(PayOrder::getIfCode, ifCode)) > 0) {
            throw new BizException("该支付接口已有服务商或商户配置参数或已发生交易，无法删除！");
        }

        boolean result = payInterfaceDefineService.removeById(ifCode);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
        return ApiRes.ok();
    }

}
