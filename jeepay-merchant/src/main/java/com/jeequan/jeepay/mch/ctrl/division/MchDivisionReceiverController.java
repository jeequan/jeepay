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
package com.jeequan.jeepay.mch.ctrl.division;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchDivisionReceiver;
import com.jeequan.jeepay.core.entity.MchDivisionReceiverGroup;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.model.DivisionReceiverBindReqModel;
import com.jeequan.jeepay.request.DivisionReceiverBindRequest;
import com.jeequan.jeepay.response.DivisionReceiverBindResponse;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchDivisionReceiverGroupService;
import com.jeequan.jeepay.service.impl.MchDivisionReceiverService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 商户分账接收者账号关系维护
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021-08-23 11:50
 */
@RestController
@RequestMapping("api/divisionReceivers")
public class MchDivisionReceiverController extends CommonCtrl {

	@Autowired private MchDivisionReceiverService mchDivisionReceiverService;
	@Autowired private MchDivisionReceiverGroupService mchDivisionReceiverGroupService;
	@Autowired private MchAppService mchAppService;
	@Autowired private SysConfigService sysConfigService;


	/** list */
	@PreAuthorize("hasAnyAuthority( 'ENT_DIVISION_RECEIVER_LIST' )")
	@RequestMapping(value="", method = RequestMethod.GET)
	public ApiRes list() {

		MchDivisionReceiver queryObject = getObject(MchDivisionReceiver.class);

		LambdaQueryWrapper<MchDivisionReceiver> condition = MchDivisionReceiver.gw();
		condition.eq(MchDivisionReceiver::getMchNo, getCurrentMchNo());

		if(queryObject.getReceiverId() != null){
			condition.eq(MchDivisionReceiver::getReceiverId, queryObject.getReceiverId());
		}

		if(StringUtils.isNotEmpty(queryObject.getReceiverAlias())){
			condition.like(MchDivisionReceiver::getReceiverAlias, queryObject.getReceiverAlias());
		}

		if(queryObject.getReceiverGroupId() != null){
			condition.eq(MchDivisionReceiver::getReceiverGroupId, queryObject.getReceiverGroupId());
		}

		if(StringUtils.isNotEmpty(queryObject.getReceiverGroupName())){
			condition.like(MchDivisionReceiver::getReceiverGroupName, queryObject.getReceiverGroupName());
		}

		if(StringUtils.isNotEmpty(queryObject.getAppId())){
			condition.like(MchDivisionReceiver::getAppId, queryObject.getAppId());
		}

		if(queryObject.getState() != null){
			condition.eq(MchDivisionReceiver::getState, queryObject.getState());
		}

		condition.orderByDesc(MchDivisionReceiver::getCreatedAt); //时间倒序

		IPage<MchDivisionReceiver> pages = mchDivisionReceiverService.page(getIPage(true), condition);
		return ApiRes.page(pages);
	}


	/** detail */
	@PreAuthorize("hasAuthority( 'ENT_DIVISION_RECEIVER_VIEW' )")
	@RequestMapping(value="/{recordId}", method = RequestMethod.GET)
	public ApiRes detail(@PathVariable("recordId") Long recordId) {
		MchDivisionReceiver record = mchDivisionReceiverService
				.getOne(MchDivisionReceiver.gw()
						.eq(MchDivisionReceiver::getMchNo, getCurrentMchNo())
						.eq(MchDivisionReceiver::getReceiverId, recordId));
		if (record == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
		return ApiRes.ok(record);
	}

	/** add */
	@PreAuthorize("hasAuthority( 'ENT_DIVISION_RECEIVER_ADD' )")
	@RequestMapping(value="", method = RequestMethod.POST)
	@MethodLog(remark = "新增分账接收账号")
	public ApiRes add() {

		DivisionReceiverBindReqModel model = getObject(DivisionReceiverBindReqModel.class);

		MchApp mchApp = mchAppService.getById(model.getAppId());
		if(mchApp == null || mchApp.getState() != CS.PUB_USABLE || !mchApp.getMchNo().equals(getCurrentMchNo()) ){
			throw new BizException("商户应用不存在或不可用");
		}

		DivisionReceiverBindRequest request = new DivisionReceiverBindRequest();
		request.setBizModel(model);
		model.setMchNo(this.getCurrentMchNo());
		model.setAppId(mchApp.getAppId());
		model.setDivisionProfit(new BigDecimal(model.getDivisionProfit()).divide(new BigDecimal(100)).toPlainString());

		JeepayClient jeepayClient = new JeepayClient(sysConfigService.getDBApplicationConfig().getPaySiteUrl(), mchApp.getAppSecret());

		try {
			DivisionReceiverBindResponse response = jeepayClient.execute(request);
			if(response.getCode() != 0){
				throw new BizException(response.getMsg());
			}
			return ApiRes.ok(response.get());
		} catch (JeepayException e) {
			throw new BizException(e.getMessage());
		}
	}

	/** update */
	@PreAuthorize("hasAuthority( 'ENT_DIVISION_RECEIVER_EDIT' )")
	@RequestMapping(value="/{recordId}", method = RequestMethod.PUT)
	@MethodLog(remark = "更新分账接收账号")
	public ApiRes update(@PathVariable("recordId") Long recordId) {

		// 请求参数
		MchDivisionReceiver reqReceiver = getObject(MchDivisionReceiver.class);

		MchDivisionReceiver record = new MchDivisionReceiver();
		record.setReceiverAlias(reqReceiver.getReceiverAlias());
		record.setReceiverGroupId(reqReceiver.getReceiverGroupId());
		record.setState(reqReceiver.getState());

		// 改为真实比例
		if(reqReceiver.getDivisionProfit() != null){
			record.setDivisionProfit(reqReceiver.getDivisionProfit().divide(new BigDecimal(100)));
		}

		if(record.getReceiverGroupId() != null){
			MchDivisionReceiverGroup groupRecord = mchDivisionReceiverGroupService.findByIdAndMchNo(record.getReceiverGroupId(), getCurrentMchNo());
			if (record == null) {
				throw new BizException("账号组不存在");
			}
			record.setReceiverGroupId(groupRecord.getReceiverGroupId());
			record.setReceiverGroupName(groupRecord.getReceiverGroupName());
		}

		LambdaUpdateWrapper<MchDivisionReceiver> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(MchDivisionReceiver::getReceiverId, recordId);
		updateWrapper.eq(MchDivisionReceiver::getMchNo, getCurrentMchNo());
		mchDivisionReceiverService.update(record, updateWrapper);
		return ApiRes.ok();
	}

	/** delete */
	@PreAuthorize("hasAuthority('ENT_DIVISION_RECEIVER_DELETE')")
	@RequestMapping(value="/{recordId}", method = RequestMethod.DELETE)
	@MethodLog(remark = "删除分账接收账号")
	public ApiRes del(@PathVariable("recordId") Long recordId) {
		MchDivisionReceiver record = mchDivisionReceiverService.getOne(MchDivisionReceiver.gw()
				.eq(MchDivisionReceiver::getReceiverGroupId, recordId).eq(MchDivisionReceiver::getMchNo, getCurrentMchNo()));
		if (record == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }

		mchDivisionReceiverService.removeById(recordId);
		return ApiRes.ok();
	}


}
