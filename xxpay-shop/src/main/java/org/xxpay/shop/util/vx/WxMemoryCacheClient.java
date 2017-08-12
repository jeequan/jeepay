package org.xxpay.shop.util.vx;

import org.xxpay.common.util.MyLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存工具类；
 * 目前使用 服务器内存的方式；
 * 
 * 1、开发者可以根据自己的需求使用不同的缓存方式,比如memcached
 * 2、系统默认使用单个公众账号的缓存处理，如果有多个账号，请开发者自行处理
 * 
 */
public class WxMemoryCacheClient {

	private static final MyLog _log = MyLog.getLog(WxMemoryCacheClient.class);

	//服务器内存的方式缓存account、accessToken、jsTicket
	private static Map<String,MpAccount> mpAccountMap = new HashMap<String,MpAccount>();
	private static Map<String,AccessToken> accountAccessTokenMap = new HashMap<String,AccessToken>();
	private static Map<String,JSTicket> accountJSTicketMap = new HashMap<String,JSTicket>();

	//微信OAuth认证的时候，服务器内存的方式缓存openid; key=sessionid ，value=openid

	private static Map<String,OAuthAccessToken> accountOAuthTokenMap = new HashMap<String,OAuthAccessToken>();


	public static void addMpAccount(MpAccount account){
		if(account != null && !mpAccountMap.containsKey(account.getAccount())){
			mpAccountMap.put(account.getAccount(), account);
		}
	}

	public static MpAccount getMpAccount(String account){
		return mpAccountMap.get(account);
	}

	//获取唯一的公众号,如果需要多账号，请自行处理
	public static MpAccount getSingleMpAccount(){
		MpAccount sigleAccount = null;
		for(String key : mpAccountMap.keySet()){
			sigleAccount = mpAccountMap.get(key);
			break;
		}
		return sigleAccount;
	}

	public static AccessToken addAccessToken(String account ,AccessToken token){
		if(token != null){
			accountAccessTokenMap.put(account, token);

//			JSONObject tokenObject = new JSONObject();
//			tokenObject.put("access_token", token.getAccessToken());
//			tokenObject.put("expires_in", token.getExpiresIn());
//			tokenObject.put("create_time", token.getCreateTime());
//			MyJedis.set(ConstUtil.RedisCache.REDIS_CACHE_NAME_ROOM, ConstUtil.RedisKey.VVLIVE_VX_ACCESS_TOKEN + account, tokenObject.toJSONString(), token.getExpiresIn());
//			_log.info("将access_token保存到redis中,key={},value={},expires={}", ConstUtil.RedisKey.VVLIVE_VX_ACCESS_TOKEN + account, tokenObject.toJSONString(), token.getExpiresIn());

		}
		return token;
	}

	/**
	 * accessToken的获取，绝对不要从缓存中直接获取，请从WxApiClient中获取；
	 * @param account
	 * @return
	 */
	public static AccessToken getAccessToken(String account){
		return accountAccessTokenMap.get(account);
	}

	/**
	 * 获取唯一的公众号的accessToken,如果需要多账号，请自行处理
	 * accessToken的获取，绝对不要从缓存中直接获取，请从WxApiClient中获取；
	 * @return
	 */
	public static AccessToken getSingleAccessToken(){
		AccessToken accessToken = null;
		for(String key : accountAccessTokenMap.keySet()){
			accessToken = accountAccessTokenMap.get(key);

			// 从redis中取
//			String tokenJson = MyJedis.get(ConstUtil.RedisCache.REDIS_CACHE_NAME_ROOM, ConstUtil.RedisKey.VVLIVE_VX_ACCESS_TOKEN + key);
//			_log.info("从redis中获取access_token,key={},value={}", ConstUtil.RedisKey.VVLIVE_VX_ACCESS_TOKEN + key, tokenJson);
//			if(StringUtils.isNotBlank(tokenJson)) {
//				JSONObject tokenObject = JSON.parseObject(tokenJson);
//				accessToken.setAccessToken(tokenObject.getString("access_token"));
//				accessToken.setExpiresIn(tokenObject.getInteger("expires_in"));
//				accessToken.setCreateTime(tokenObject.getLong("create_time"));
//			}

			if (accessToken != null) break;
		}
		return accessToken;
	}

	/**
	 * 添加JSTicket到缓存
	 * @param account
	 * @param jsTicket
	 * @return
	 */
	public static JSTicket addJSTicket(String account , JSTicket jsTicket){
		if(jsTicket != null){
			accountJSTicketMap.put(account, jsTicket);
		}
		return jsTicket;
	}

	/**
	 * JSTicket的获取，绝对不要从缓存中直接获取，请从JSTicket中获取；
	 * @param account
	 * @return
	 */
	public static JSTicket getJSTicket(String account){
		return accountJSTicketMap.get(account);
	}

	/**
	 * 获取唯一的公众号的JSTicket,如果需要多账号，请自行处理
	 * JSTicket的获取，绝对不要从缓存中直接获取，请从WxApiClient中获取；
	 * @return
	 */
	public static JSTicket getSingleJSTicket(){
		JSTicket jsTicket = null;
		for(String key : accountJSTicketMap.keySet()){
			jsTicket = accountJSTicketMap.get(key);
			break;
		}
		return jsTicket;
	}




	//处理OAuth的Token
	public static AccessToken addOAuthAccessToken(String account ,OAuthAccessToken token){
		if(token != null){
			accountOAuthTokenMap.put(account, token);
		}
		return token;
	}

	/**
	 * OAuthAccessToken的获取，绝对不要从缓存中直接获取，请从WxApiClient中获取；
	 * @param account
	 * @return
	 */
	public static OAuthAccessToken getOAuthAccessToken(String account){
		return accountOAuthTokenMap.get(account);
	}

	/**
	 * 获取唯一的公众号的accessToken,如果需要多账号，请自行处理
	 * OAuthAccessToken的获取，绝对不要从缓存中直接获取，请从WxApiClient中获取；
	 * @return
	 */
	public static OAuthAccessToken getSingleOAuthAccessToken(){
		OAuthAccessToken token = null;
		for(String key : accountOAuthTokenMap.keySet()){
			token = accountOAuthTokenMap.get(key);
			break;
		}
		return token;
	}
	
	
}


