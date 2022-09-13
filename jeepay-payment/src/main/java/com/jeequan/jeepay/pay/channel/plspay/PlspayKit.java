package com.jeequan.jeepay.pay.channel.plspay;

import com.jeequan.jeepay.Jeepay;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.model.params.plspay.PlspayConfig;
import com.jeequan.jeepay.core.model.params.plspay.PlspayNormalMchParams;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.request.PayOrderCreateRequest;
import com.jeequan.jeepay.response.JeepayResponse;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/*
 * 工具类
 *
 * @author xiaoyu
 * @site https://www.jeequan.com
 * @date 2022/8/23 16:29
 */
@Slf4j
public class PlspayKit {


	public static PayOrderCreateResponse payRequest(PayOrder payOrder, MchAppConfigContext mchAppConfigContext, PayOrderCreateReqModel model) throws JeepayException {

		// 发起统一下单
		PayOrderCreateResponse response = new PayOrderCreateResponse();
		ConfigContextQueryService configContextQueryService = SpringBeansUtil.getBean(ConfigContextQueryService.class);
		PlspayNormalMchParams normalMchParams = (PlspayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.PLSPAY);
		// 构建请求数据
		PayOrderCreateRequest request = new PayOrderCreateRequest();
		model.setMchNo(normalMchParams.getMerchantNo());        // 商户号
		model.setAppId(normalMchParams.getAppId());             // 应用ID
		model.setMchOrderNo(payOrder.getPayOrderId());          // 商户订单号
		model.setAmount(payOrder.getAmount());                  // 金额，单位分
		model.setCurrency(payOrder.getCurrency());              // 币种，目前只支持cny
		model.setClientIp(payOrder.getClientIp());              // 发起支付请求客户端的IP地址
		model.setSubject(payOrder.getSubject());                // 商品标题
		model.setBody(payOrder.getBody());                      // 商品描述
		request.setBizModel(model);

		if (normalMchParams.getSignType().equals(PlspayConfig.DEFAULT_SIGN_TYPE) || StringUtils.isEmpty(normalMchParams.getSignType())) {
			JeepayClient jeepayClient = JeepayClient.getInstance(normalMchParams.getAppId(), normalMchParams.getAppSecret(), Jeepay.getApiBase());
			response = jeepayClient.execute(request);

		} else if (normalMchParams.getSignType().equals(PlspayConfig.SIGN_TYPE_RSA2)) {
			JeepayClient jeepayClient = JeepayClient.getInstance(normalMchParams.getAppId(), normalMchParams.getRsa2AppPrivateKey(), Jeepay.getApiBase());
			response = jeepayClient.executeByRSA2(request);
		}
		return response;
	}

	public static Boolean checkPayResp(JeepayResponse response , MchAppConfigContext mchAppConfigContext) {
		ConfigContextQueryService configContextQueryService = SpringBeansUtil.getBean(ConfigContextQueryService.class);
		PlspayNormalMchParams normalMchParams = (PlspayNormalMchParams) configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), CS.IF_CODE.PLSPAY);

		boolean isSuccess = false;
		if (normalMchParams.getSignType().equals(PlspayConfig.DEFAULT_SIGN_TYPE) || StringUtils.isEmpty(normalMchParams.getSignType())) {
			isSuccess = response.isSuccess(normalMchParams.getAppSecret());

		} else if (normalMchParams.getSignType().equals(PlspayConfig.SIGN_TYPE_RSA2)) {
			isSuccess = response.isSuccessByRsa2(normalMchParams.getRsa2PayPublicKey());
		}

		return isSuccess;
	}

}
