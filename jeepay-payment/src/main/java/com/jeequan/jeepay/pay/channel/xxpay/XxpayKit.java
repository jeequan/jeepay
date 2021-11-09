package com.jeequan.jeepay.pay.channel.xxpay;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/*
 * 小新支付 工具类
 *
 * @author jmdhappy
 * @site https://www.jeequan.com
 * @date 2021/9/20 10:09
 */
@Slf4j
public class XxpayKit {
	
	private static String encodingCharset = "UTF-8";

	/**
	 * 计算签名
	 * @param map
	 * @param key
	 * @return
	 */
	public static String getSign(Map<String,Object> map, String key){
		ArrayList<String> list = new ArrayList<String>();
		for(Map.Entry<String,Object> entry:map.entrySet()){
			if(null != entry.getValue() && !"".equals(entry.getValue())){
				list.add(entry.getKey() + "=" + entry.getValue() + "&");
			}
		}
		int size = list.size();
		String [] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < size; i ++) {
			sb.append(arrayToSort[i]);
		}
		String result = sb.toString();
		result += "key=" + key;
		log.info("signStr:{}", result);
		result = md5(result, encodingCharset).toUpperCase();
		return result;
	}


	/**
	 * 计算MD5值
	 * @param value
	 * @param charset
	 * @return
	 */
	public static String md5(String value, String charset) {
		MessageDigest md;
		try {
			byte[] data = value.getBytes(charset);
			md = MessageDigest.getInstance("MD5");
			byte[] digestData = md.digest(data);
			return toHex(digestData);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String toHex(byte input[]) {
		if (input == null) {
			return null;
		}
		StringBuffer output = new StringBuffer(input.length * 2);
		for (int i = 0; i < input.length; i++) {
			int current = input[i] & 0xff;
			if (current < 16){
				output.append("0");
			}
			output.append(Integer.toString(current, 16));
		}
		return output.toString();
	}

	public static String getPaymentUrl(String payUrl) {
		return getPayUrl(payUrl) + "api/pay/create_order";
	}

	public static String getQueryPayOrderUrl(String payUrl) {
		return getPayUrl(payUrl) + "api/pay/query_order";
	}

	public static String getRefundUrl(String payUrl) {
		return getPayUrl(payUrl) + "api/refund/create_order";
	}

	public static String getQueryRefundOrderUrl(String payUrl) {
		return getPayUrl(payUrl) + "api/refund/query_order";
	}

	protected static String getPayUrl(String payUrl) {
		if(StringUtils.isEmpty(payUrl)) {
			return payUrl;
		}
		if(!payUrl.endsWith("/")) {
			payUrl += "/";
		}
		return payUrl;
	}

}
