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
package com.jeequan.jeepay.pay.channel.ysfpay.utils;

import com.alibaba.fastjson.JSON;
import com.jeequan.jeepay.core.constants.CS;

import javax.activation.MimetypesFileTypeMap;
import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/*
* 参考云闪付demo实现的 httputils
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:25
*/
public class YsfHttpUtil {

	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final int DEFAULT_TIMEOUT = 60 * 1000; // 60 秒超时

	public static String doGet(String url) throws Exception {
		return doGet(url, null);
	}

	public static String doGet(String url, Map<String, Object> params) throws Exception {
		return doGet(url, params, null);
	}

	public static String doGet(String url, Map<String, Object> headers, Map<String, Object> params) throws Exception {
		String getUrl = buildGetUrl(url, params);
		return doRequest(getUrl, "GET", headers, null);
	}

	public static String doPost(String url) throws Exception {
		return doPost(url, null);
	}

	public static String doPost(String url, Map<String, Object> params) throws Exception {
		return doPost(url, null, params);
	}

	public static String doPost(String url, Map<String, Object> headers, Map<String, Object> params) throws Exception {
		return doPostStr(url, headers, buildQueryParams(params));
	}

	public static String doPostStr(String url, Map<String, Object> headers, String data) throws Exception {
		return doRequest(url, "POST", headers, data);
	}

	public static String doPostJson(String url, Map<String, Object> headers, Map<String, Object> params) throws Exception {
		if (headers == null) {
			headers = new HashMap<String, Object>();
		}
		if (!headers.containsKey("Content-Type")) {
			headers.put("Content-Type", "application/json; charset=" + DEFAULT_CHARSET);
		}
		return doPostStr(url, headers, JSON.toJSONString(params));
	}

	public static String doPostFile(String url, Map<String, Object> headers, Map<String, Object> params, Map<String, FileItem> fileParams) throws Exception {
		HttpURLConnection http = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			String boundary = String.valueOf(System.currentTimeMillis()); // 随机分隔线

			http = getHttpConnection(url, "POST");
			http.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary + ";charset=" + DEFAULT_CHARSET);

			// 添加HTTP请求头
			if (headers != null && !headers.isEmpty()) {
				for (Entry<String, Object> entry : headers.entrySet()) {
					http.setRequestProperty(entry.getKey(), entry.getValue().toString());
				}
			}

			out = http.getOutputStream();

			// 组装文本请求参数
			byte[] entryBoundaryBytes = ("\r\n--" + boundary + "\r\n").getBytes(DEFAULT_CHARSET);
			if (params != null && !params.isEmpty()) {
				for (Entry<String, Object> textEntry : params.entrySet()) {
					byte[] textBytes = getTextEntry(textEntry.getKey(), String.valueOf(textEntry.getValue()));
					out.write(entryBoundaryBytes);
					out.write(textBytes);
				}
			}

			// 组装文件请求参数
			if (fileParams != null && !fileParams.isEmpty()) {
				for (Entry<String, FileItem> fileEntry : fileParams.entrySet()) {
					FileItem fileItem = fileEntry.getValue();
					byte[] fileBytes = getFileEntry(fileEntry.getKey(), fileItem.getFileName(), fileItem.getMimeType());
					out.write(entryBoundaryBytes);
					out.write(fileBytes);
					out.write(fileItem.getContent());
				}
			}

			// 添加请求结束标志
			byte[] endBoundaryBytes = ("\r\n--" + boundary + "--\r\n").getBytes(DEFAULT_CHARSET);
			out.write(endBoundaryBytes);
			out.flush();

			in = http.getInputStream();
			return getStreamAsString(in);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (http != null) {
					http.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String doRequest(String url, String method, Map<String, Object> headers, String data) throws Exception {
		HttpURLConnection http = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			http = getHttpConnection(url, method);
			if (headers != null && !headers.isEmpty()) {
				for (Entry<String, Object> entry : headers.entrySet()) {
					http.setRequestProperty(entry.getKey(), entry.getValue().toString());
				}
			}
			if (data != null && !data.trim().isEmpty()) {
				out = http.getOutputStream();
				out.write(data.getBytes(DEFAULT_CHARSET));
				out.flush();
			}
			in = http.getInputStream();
			return getStreamAsString(in);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (http != null) {
					http.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static HttpURLConnection getHttpConnection(String url, String method) throws Exception {
		boolean isSSL = url.startsWith("https");
		if (isSSL) {
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(new KeyManager[0], new TrustManager[] { new SimpleTrustManager() }, new SecureRandom());
			SSLSocketFactory sslf = sslContext.getSocketFactory();

			HttpsURLConnection https = (HttpsURLConnection) new URL(url).openConnection();
			https.setHostnameVerifier(new SimpleHostnameVerifier());
			https.setSSLSocketFactory(sslf);

			https.setRequestMethod(method);
			https.setDoOutput(true);
			https.setDoInput(true);
			https.setUseCaches(false);

			https.setConnectTimeout(DEFAULT_TIMEOUT);
			https.setReadTimeout(DEFAULT_TIMEOUT);

			return https;
		} else {
			HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
			http.setRequestMethod(method);
			http.setDoOutput(true);
			http.setDoInput(true);
			http.setUseCaches(false);

			http.setConnectTimeout(DEFAULT_TIMEOUT);
			http.setReadTimeout(DEFAULT_TIMEOUT);

			return http;
		}
	}

	private static String getStreamAsString(InputStream in) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in, DEFAULT_CHARSET));
			StringBuilder buffer = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			return buffer.toString();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private static String buildGetUrl(String url, Map<String, Object> params) throws Exception {
		String queryParams = buildQueryParams(params);
		if (null == queryParams || queryParams.isEmpty()) {
			return url;
		}
		StringBuilder sb = new StringBuilder();
		if (url.endsWith("?")) {
			sb.append(url).append(queryParams);
		} else {
			sb.append(url).append("?").append(queryParams);
		}
		return sb.toString();
	}

	private static String buildQueryParams(Map<String, Object> params) throws Exception {
		if (null == params || params.isEmpty()) {
			return null;
		}
		StringBuilder query = new StringBuilder();
		boolean hasParam = false;
		for (Entry<String, Object> entry : params.entrySet()) {
			Object v = entry.getValue();
			if (v == null) {
				continue;
			}
			if (hasParam) {
				query.append("&");
			} else {
				hasParam = true;
			}
			query.append(entry.getKey()).append("=").append(URLEncoder.encode(v.toString(), DEFAULT_CHARSET));
		}
		return query.toString();
	}

	private static byte[] getTextEntry(String fieldName, String fieldValue) throws Exception {
		StringBuilder entry = new StringBuilder();
		entry.append("Content-Disposition:form-data; name=\"");
		entry.append(fieldName);
		entry.append("\"\r\nContent-Type:text/plain\r\n\r\n");
		entry.append(fieldValue);
		return entry.toString().getBytes(DEFAULT_CHARSET);
	}

	private static byte[] getFileEntry(String fieldName, String fileName, String mimeType) throws Exception {
		StringBuilder entry = new StringBuilder();
		entry.append("Content-Disposition:form-data; name=\"");
		entry.append(fieldName);
		entry.append("\"; filename=\"");
		entry.append(fileName);
		entry.append("\"\r\nContent-Type:");
		entry.append(mimeType);
		entry.append("\r\n\r\n");
		return entry.toString().getBytes(DEFAULT_CHARSET);
	}

	private static class SimpleHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	private static class SimpleTrustManager implements X509TrustManager {
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	}


	public static class FileItem {

		private String fileName;
		private String mimeType;
		private byte[] content;

		public FileItem() {
		}

		public FileItem(String fileName, byte[] content) {
			this.fileName = fileName;
			this.content = content;
		}

		public FileItem(String fileName, byte[] content, String mimeType) {
			this.fileName = fileName;
			this.content = content;
			this.mimeType = mimeType;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getMimeType() {
			if (mimeType == null) {
				mimeType = getContentType(fileName);
			}
			return mimeType;
		}

		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}

		public byte[] getContent() {
			return content;
		}

		public void setContent(byte[] content) {
			this.content = content;
		}

		private String getContentType(String fileName) {
			String defaultType = "application/octet-stream";
			if (fileName == null || fileName.isEmpty()) {
				return defaultType;
			}
			String contentType = null;
			try {
				Path path = Paths.get(fileName);
				contentType = Files.probeContentType(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (contentType == null || contentType.isEmpty()) {
				contentType = new MimetypesFileTypeMap().getContentType(fileName);
			}
			return contentType;
		}
	}

	/** 云闪付条码付 封装参数，orderType **/
	public static String getOrderTypeByBar(String wayCode){

		if(CS.PAY_WAY_CODE.ALI_BAR.equals(wayCode)){
			return "alipay";

		}else if(CS.PAY_WAY_CODE.WX_BAR.equals(wayCode)){
			return "wechat";

		}else if(CS.PAY_WAY_CODE.YSF_BAR.equals(wayCode)){
			return "unionpay";
		}

		return null;
	}

	/** 云闪付jsapi对应的订单类型 **/
	public static String getOrderTypeByJSapi(String wayCode){

		if(CS.PAY_WAY_CODE.ALI_JSAPI.equals(wayCode)){
			return "alipayJs";

		}else if(CS.PAY_WAY_CODE.WX_JSAPI.equals(wayCode)){
			return "wechatJs";

		}else if(CS.PAY_WAY_CODE.YSF_JSAPI.equals(wayCode)){
			return "upJs";
		}

		return null;
	}

	/** 云闪付通用订单类型， 如查单 **/
	public static String getOrderTypeByCommon(String wayCode){

		if(CS.PAY_WAY_CODE.ALI_JSAPI.equals(wayCode) || CS.PAY_WAY_CODE.ALI_BAR.equals(wayCode)){
			return "alipay";

		}else if(CS.PAY_WAY_CODE.WX_JSAPI.equals(wayCode) || CS.PAY_WAY_CODE.WX_BAR.equals(wayCode)){
			return "wechat";

		}else if(CS.PAY_WAY_CODE.YSF_JSAPI.equals(wayCode) || CS.PAY_WAY_CODE.YSF_BAR.equals(wayCode)){
			return "unionpay";
		}
		return null;
	}



}
