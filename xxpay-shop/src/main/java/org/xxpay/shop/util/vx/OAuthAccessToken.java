package org.xxpay.shop.util.vx;

/**
 * OAuth token
 */
public class OAuthAccessToken extends AccessToken {
	
	//oauth2.0
	private String oauthAccessToken;//刷新token
	private String openid;
	private String scope;
	
	
	public String getOauthAccessToken() {
		return oauthAccessToken;
	}
	public void setOauthAccessToken(String oauthAccessToken) {
		this.oauthAccessToken = oauthAccessToken;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	
}

