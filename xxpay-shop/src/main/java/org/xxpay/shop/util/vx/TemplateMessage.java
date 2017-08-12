package org.xxpay.shop.util.vx;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * 发送的模板消息对象
 */
public class TemplateMessage {

	private String openid;//粉丝id
	private String templateId;//模板id
	private String url;//链接
	private String color = "#173177";//颜色
	private Map<String,String> dataMap;//参数数据
	
	
	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, String> dataMap) {
		this.dataMap = dataMap;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	@Override
	public String toString(){
		JSONObject jsObj = new JSONObject();
		jsObj.put("touser", openid);
		jsObj.put("template_id", templateId);
		jsObj.put("url", url);
		
		JSONObject data = new JSONObject();
		if(dataMap != null){
			for(String key : dataMap.keySet()){
				JSONObject item = new JSONObject();
				item.put("value", dataMap.get(key));
				item.put("color", color);
				data.put(key,item);
			}
		}
		jsObj.put("data", data);
		return jsObj.toString();
	}
	
	
}
