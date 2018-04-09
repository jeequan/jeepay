package org.xxpay.common.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-07-05
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
public class PayDigestUtil {

	private static final MyLog _log = MyLog.getLog(PayDigestUtil.class);
	private static String encodingCharset = "UTF-8";
	
	/**
	 * @param aValue
	 * @param aKey
	 * @return
	 */
	public static String hmacSign(String aValue, String aKey) {
		byte k_ipad[] = new byte[64];
		byte k_opad[] = new byte[64];
		byte keyb[];
		byte value[];
		try {
			keyb = aKey.getBytes(encodingCharset);
			value = aValue.getBytes(encodingCharset);
		} catch (UnsupportedEncodingException e) {
			keyb = aKey.getBytes();
			value = aValue.getBytes();
		}

		Arrays.fill(k_ipad, keyb.length, 64, (byte) 54);
		Arrays.fill(k_opad, keyb.length, 64, (byte) 92);
		for (int i = 0; i < keyb.length; i++) {
			k_ipad[i] = (byte) (keyb[i] ^ 0x36);
			k_opad[i] = (byte) (keyb[i] ^ 0x5c);
		}

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {

			return null;
		}
		md.update(k_ipad);
		md.update(value);
		byte dg[] = md.digest();
		md.reset();
		md.update(k_opad);
		md.update(dg, 0, 16);
		dg = md.digest();
		return toHex(dg);
	}

	public static String toHex(byte input[]) {
		if (input == null)
			return null;
		StringBuffer output = new StringBuffer(input.length * 2);
		for (int i = 0; i < input.length; i++) {
			int current = input[i] & 0xff;
			if (current < 16)
				output.append("0");
			output.append(Integer.toString(current, 16));
		}

		return output.toString();
	}

	/**
	 * 
	 * @param args
	 * @param key
	 * @return
	 */
	public static String getHmac(String[] args, String key) {
		if (args == null || args.length == 0) {
			return (null);
		}
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			str.append(args[i]);
		}
		return (hmacSign(str.toString(), key));
	}

	/**
	 * @param aValue
	 * @return
	 */
	public static String digest(String aValue) {
		aValue = aValue.trim();
		byte value[];
		try {
			value = aValue.getBytes(encodingCharset);
		} catch (UnsupportedEncodingException e) {
			value = aValue.getBytes();
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		return toHex(md.digest(value));

	}
	
	public static String md5(String value, String charset) {
		MessageDigest md = null;
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

	public static String getSign(Object o, String key) throws IllegalAccessException {
		if(o instanceof Map) {
			return getSign((Map<String, Object>)o, key);
		}
		ArrayList<String> list = new ArrayList<String>();
		Class cls = o.getClass();
		Field[] fields = cls.getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			if (f.get(o) != null && !"".equals(f.get(o))) {
				list.add(f.getName() + "=" + f.get(o) + "&");
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
		_log.debug("Sign Before MD5:" + result);
		result = md5(result, encodingCharset).toUpperCase();
		_log.debug("Sign Result:" + result);
		return result;
	}

	public static String getSign(Map<String,Object> map, String key){
		ArrayList<String> list = new ArrayList<String>();
		for(Map.Entry<String,Object> entry:map.entrySet()){
			if(!"".equals(entry.getValue()) && null != entry.getValue()){
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
		_log.debug("Sign Before MD5:" + result);
		result = md5(result, encodingCharset).toUpperCase();
		_log.debug("Sign Result:" + result);
		return result;
	}

	/**
	 *
	 * @param map
	 * @param key
	 * @param notContains 不包含的签名字段
     * @return
     */
	public static String getSign(Map<String,Object> map, String key, String... notContains){
		Map<String,Object> newMap = new HashMap<String,Object>();
		for(Map.Entry<String,Object> entry:map.entrySet()){
			boolean isContain = false;
			for(int i=0; i<notContains.length; i++) {
				if(entry.getKey().equals(notContains[i])) {
					isContain = true;
					break;
				}
			}
			if(!isContain) {
				newMap.put(entry.getKey(), entry.getValue());
			}
		}
		return getSign(newMap, key);
	}

	public static void main(String[] args) {
		String key = "8UPp0KE8sq73zVP370vko7C39403rtK1YwX40Td6irH216036H27Eb12792t";
		String dataStr = "AnnulCard1000043252120080620160450.0http://localhost/SZXpro/callback.asp这4564868265473632445648682654736324511";
		System.out.println(hmacSign(dataStr, key));
		
		System.out.println(md5(dataStr, "UTF-8"));
		System.out.println(md5(dataStr, "GBK"));
	}
}
