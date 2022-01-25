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

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.mq.model.PayOrderDivisionMQ;
import com.jeequan.jeepay.components.mq.vender.IMQSender;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayOrderDivisionRecord;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.PayOrderDivisionRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分账记录
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021-08-25 11:50
 */
@RestController
@RequestMapping("api/division/records")
public class PayOrderDivisionRecordController extends CommonCtrl {

	@Autowired private PayOrderDivisionRecordService payOrderDivisionRecordService;
	@Autowired private IMQSender mqSender;


	/** list */
	@PreAuthorize("hasAnyAuthority( 'ENT_DIVISION_RECORD_LIST' )")
	@RequestMapping(value="", method = RequestMethod.GET)
	public ApiRes list() {

		PayOrderDivisionRecord queryObject = getObject(PayOrderDivisionRecord.class);
		JSONObject paramJSON = getReqParamJSON();

		LambdaQueryWrapper<PayOrderDivisionRecord> condition = PayOrderDivisionRecord.gw();
		condition.eq(PayOrderDivisionRecord::getMchNo, getCurrentMchNo());

		if(queryObject.getReceiverId() != null){
			condition.eq(PayOrderDivisionRecord::getReceiverId, queryObject.getReceiverId());
		}

		if(queryObject.getReceiverGroupId() != null){
			condition.eq(PayOrderDivisionRecord::getReceiverGroupId, queryObject.getReceiverGroupId());
		}

		if(StringUtils.isNotEmpty(queryObject.getAppId())){
			condition.like(PayOrderDivisionRecord::getAppId, queryObject.getAppId());
		}

		if(queryObject.getState() != null){
			condition.eq(PayOrderDivisionRecord::getState, queryObject.getState());
		}

		if(StringUtils.isNotEmpty(queryObject.getPayOrderId())){
			condition.eq(PayOrderDivisionRecord::getPayOrderId, queryObject.getPayOrderId());
		}

		if(StringUtils.isNotEmpty(queryObject.getAccNo())){
			condition.eq(PayOrderDivisionRecord::getAccNo, queryObject.getAccNo());
		}

		if (paramJSON != null) {
			if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
				condition.ge(PayOrderDivisionRecord::getCreatedAt, paramJSON.getString("createdStart"));
			}
			if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
				condition.le(PayOrderDivisionRecord::getCreatedAt, paramJSON.getString("createdEnd"));
			}
		}

		condition.orderByDesc(PayOrderDivisionRecord::getCreatedAt); //时间倒序

		IPage<PayOrderDivisionRecord> pages = payOrderDivisionRecordService.page(getIPage(true), condition);
		return ApiRes.page(pages);
	}


	/** detail */
	@PreAuthorize("hasAuthority( 'ENT_DIVISION_RECORD_VIEW' )")
	@RequestMapping(value="/{recordId}", method = RequestMethod.GET)
	public ApiRes detail(@PathVariable("recordId") Long recordId) {
		PayOrderDivisionRecord record = payOrderDivisionRecordService
				.getOne(PayOrderDivisionRecord.gw()
						.eq(PayOrderDivisionRecord::getMchNo, getCurrentMchNo())
						.eq(PayOrderDivisionRecord::getRecordId, recordId));
		if (record == null) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
		return ApiRes.ok(record);
	}



	/** 分账接口重试 */
	@PreAuthorize("hasAuthority( 'ENT_DIVISION_RECORD_RESEND' )")
	@RequestMapping(value="/resend/{recordId}", method = RequestMethod.POST)
	public ApiRes resend(@PathVariable("recordId") Long recordId) {
		PayOrderDivisionRecord record = payOrderDivisionRecordService
				.getOne(PayOrderDivisionRecord.gw()
						.eq(PayOrderDivisionRecord::getMchNo, getCurrentMchNo())
						.eq(PayOrderDivisionRecord::getRecordId, recordId));
		if (record == null) {
			throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
		}

		if(record.getState() != PayOrderDivisionRecord.STATE_FAIL){
			throw new BizException("请选择失败的分账记录");
		}

		// 更新订单状态 & 记录状态
		payOrderDivisionRecordService.updateResendState(record.getPayOrderId());

		// 重发到MQ
		mqSender.send(PayOrderDivisionMQ.build(record.getPayOrderId(), null, null, true));

		return ApiRes.ok(record);
	}


}
