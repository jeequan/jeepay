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
package com.jeequan.jeepay.mgr.ctrl.config;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.components.mq.model.ResetAppConfigMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.SysConfig;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 系统配置信息类
 *
 * @author pangxiaoyu
 * @site https://www.jeequan.com
 * @date 2021-06-07 07:15
 */
@Tag(name = "系统管理（配置信息类）")
@Slf4j
@RestController
@RequestMapping("api/sysConfigs")
public class SysConfigController extends CommonCtrl {

	@Autowired private SysConfigService sysConfigService;
	@Autowired private IMQSender mqSender;


	/**
	 * @author: pangxiaoyu
	 * @date: 2021/6/7 16:19
	 * @describe: 分组下的配置
	 */
	@Operation(summary = "系统配置--查询分组下的配置",description = "")
	@Parameters({
			@Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
			@Parameter(name = "groupKey", description = "分组key")
	})
	@PreAuthorize("hasAuthority('ENT_SYS_CONFIG_INFO')")
	@RequestMapping(value="/{groupKey}", method = RequestMethod.GET)
	public ApiRes<List<SysConfig>> getConfigs(@PathVariable("groupKey") String groupKey) {
		LambdaQueryWrapper<SysConfig> condition = SysConfig.gw();
		condition.orderByAsc(SysConfig::getSortNum);
		if(StringUtils.isNotEmpty(groupKey)){
			condition.eq(SysConfig::getGroupKey, groupKey);
		}
		List<SysConfig> configList = sysConfigService.list(condition);
		//返回数据
		return ApiRes.ok(configList);
	}

	/**
	 * @author: pangxiaoyu
	 * @date: 2021/6/7 16:19
	 * @describe: 系统配置修改
	 */
	@Operation(summary = "系统配置--修改分组下的配置",description = "")
	@Parameters({
			@Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
			@Parameter(name = "groupKey", description = "分组key", required = true),
			@Parameter(name = "mchSiteUrl", description = "商户平台网址(不包含结尾/)"),
			@Parameter(name = "mgrSiteUrl", description = "运营平台网址(不包含结尾/)"),
			@Parameter(name = "ossPublicSiteUrl", description = "公共oss访问地址(不包含结尾/)"),
			@Parameter(name = "paySiteUrl", description = "支付网关地址(不包含结尾/)")
	})
	@PreAuthorize("hasAuthority('ENT_SYS_CONFIG_EDIT')")
	@MethodLog(remark = "系统配置修改")
	@RequestMapping(value="/{groupKey}", method = RequestMethod.PUT)
	public ApiRes update(@PathVariable("groupKey") String groupKey) {
		JSONObject paramJSON = getReqParamJSON();
		Map<String, String> updateMap = JSONObject.toJavaObject(paramJSON, Map.class);
		int update = sysConfigService.updateByConfigKey(updateMap);
		if(update <= 0) {
            return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR, "更新失败");
        }

		// 异步更新到MQ
		SpringBeansUtil.getBean(SysConfigController.class).updateSysConfigMQ(groupKey);

		return ApiRes.ok();
	}

	@Async
	public void updateSysConfigMQ(String groupKey){
		mqSender.send(ResetAppConfigMQ.build(groupKey));
	}


}
