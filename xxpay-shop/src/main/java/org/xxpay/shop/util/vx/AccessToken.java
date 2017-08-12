package org.xxpay.shop.util.vx;


import org.xxpay.shop.util.CalendarUtil;

public class AccessToken {
	private String accessToken;// 接口访问凭证
	private int expiresIn;// 凭证有效期，单位：秒
	private long createTime;//创建时间，单位：秒 ，用于判断是否过期
	
	private Integer errcode;//错误编码
	private String errmsg;//错误消息
	
	public AccessToken(){
		this.createTime = CalendarUtil.getTimeInSeconds();
	}
	
	public AccessToken(String accessToken, int expiresIn){
		this.accessToken = accessToken;
		this.expiresIn = expiresIn;
		this.createTime = CalendarUtil.getTimeInSeconds();
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public int getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}
	public Integer getErrcode() {
		return errcode;
	}
	public void setErrcode(Integer errcode) {
		this.errcode = errcode;
		this.errmsg = ErrCode.errMsg(errcode);
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
	
	/**
	 * 是否超时，微信默认7200s超时
	 * @return true-超时；false-没有超时
	 */
	public boolean isExpires(){
		long now = CalendarUtil.getTimeInSeconds();
		return now - this.createTime - 10 >= this.expiresIn; //预留 10s 
	}
	
	/**
	 * 是否超时
	 * @return true-超时；false-没有超时
	 */
	public boolean isExpires(Long expireTime){
		long now = CalendarUtil.getTimeInSeconds();
		return now - this.createTime - 10 >= expireTime; //预留 10s 
	}
	
	
}
