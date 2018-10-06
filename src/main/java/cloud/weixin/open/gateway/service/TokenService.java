package cloud.weixin.open.gateway.service;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import cloud.weixin.open.gateway.Configure;
import cloud.weixin.open.gateway.Configure.AppInfo;
import cloud.weixin.open.gateway.aes.AesException;
import cloud.weixin.open.gateway.aes.WXBizMsgCrypt;
import cloud.weixin.open.gateway.data.AppMsg;
import cloud.weixin.open.gateway.data.AuthMsg;
import cloud.weixin.open.gateway.data.EncryptMsg;
import gaf2.core.exception.BusinessError;
import reactor.core.publisher.Mono;

@Service
public class TokenService {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Configure config;

	private CacheService cache;

	private WeixinAPI api;
	
	private int expires_off = 300;

	@Autowired
	public TokenService(Configure config, CacheService cache, WeixinAPI api) {
		this.config = config;
		this.cache = cache;
		this.api = api;
	}

	/**
	 * 获得三方平台的AccessToken
	 * 
	 * @return
	 */
	public Mono<String> componentAccessToken() {

		// TODO: 从cache中获取token
		String token = this.cache.getCache(compTokenKey());
		if (token != null) {
			return Mono.just(token);
		}

		String ticket = this.cache.getCache(compTicketKey());
		if (ticket == null) {
			throw new BusinessError(BusinessError.ERR_SERVICE_FAULT, "verify ticket not found!");
		}

		// TODO: 通过接口获得component_access_token
		AppInfo appInfo = config.getComponent();
		return this.api.componentAccessToken(appInfo.getAppId(), appInfo.getSecret(), ticket)
				.map(compAccessToken -> {
					// TODO: 保存component_access_token到cache
					String res = compAccessToken.getComponent_access_token();
					int expire_in = compAccessToken.getExpires_in();
					this.cache.setCache(compTokenKey(), res, expire_in - expires_off);
					return res;
				});
	}

	/**
	 * 获得预授权码
	 * 
	 * @param appId 要授权的公众号appId
	 * @return
	 */
	public Mono<String> preAuthCode() {
		String compId = config.getComponent().getAppId();
		return this.componentAccessToken().flatMap(token -> {
			return this.api.preAuthCode(compId, token).map(res -> res.getPre_auth_code());
		});
	}

	/**
	 * 获得公众平台的AccessToken
	 */
	public Mono<String> accessToken(String appId) {

		// TODO: 从cache中获取token
		String token = this.cache.getCache(authKey(appId, "token"));
		if (token != null) {
			return Mono.just(token);
		}
		
		// TODO: 通过接口获得component_access_token
		AppInfo appInfo = config.getComponent();
		return this.componentAccessToken()
				.flatMap(compAccessToken -> {
					// TODO: refresh access token
					String refreshToken = this.cache.getCache(authKey(appId, "refresh"));
					if(refreshToken == null) {
						throw new BusinessError(BusinessError.ERR_SERVICE_FAULT, "refresh token not found!");
					}
					return this.api.apiAuthorizerToken(appInfo.getAppId(), compAccessToken, appId, refreshToken);
				}).map(authInfo -> {
					String key = authKey(appId, "token");
					String val = authInfo.getAuthorizer_access_token();
					int expires = authInfo.getExpires_in() - expires_off;
					this.cache.setCache(key, val, expires);
					key = authKey(appId, "refresh");
					val = authInfo.getAuthorizer_refresh_token();
					this.cache.setCache(key, val);
					return authInfo.getAuthorizer_access_token();
				});
	}

	/**
	 * 处理授权推送消息
	 * 
	 * @param xmlData
	 */
	@Async
	public void handleAuthMsg(String xmlData) {
		log.debug("receive component_auth_msg: \n{}", xmlData);
		try {
			AuthMsg msg = AuthMsg.fromXml(xmlData);
			String appId = msg.getAppId();
			if(msg.getEncrypt() != null) {
				AppInfo appInfo = config.getComponent();
				if (!appId.equals(appInfo.getAppId())) {
					log.error("appId与配置信息不匹配: {} <-> {}", appId, appInfo.getAppId());
					return;
				}
				String key = appInfo.getKey();
				String token = appInfo.getToken();
				WXBizMsgCrypt pc = new WXBizMsgCrypt(token, key, appId);
				String text = pc.decrypt(msg.getEncrypt());
				log.debug("decrypt component_verify_ticket result: \n{}", text);
				msg= AuthMsg.fromXml(text);
			}
			if("component_verify_ticket".equalsIgnoreCase(msg.getInfoType())) {
				String ticket = msg.getComponentVerifyTicket();
				log.debug("save component_verify_ticket: {} - {}", appId, ticket);
				this.cache.setCache(compTicketKey(), ticket);
			} else /*if ("authorized".equalsIgnoreCase(msg.getInfoType()))*/{
				log.info("component_auth_msg: infoType-{} appId-{}", msg.getInfoType(), msg.getAuthorizerAppid());
			} 
		} catch (AesException e) {
			// TODO Auto-generated catch block
			log.warn("解密component_verify_ticket失败", e);
		} catch (JAXBException e) {
			log.warn("解析component_verify_ticket失败", e);
		}
	}

	/**
	 * 处理公众号推送消息
	 * 
	 * @param xmlData
	 */
	@Async
	public void handleAppMsg(String appId, String xmlData) {
		log.debug("receive app_msg: \n{}", xmlData);
		try {
			xmlData = decryptMsg(xmlData);
			AppMsg msg = AppMsg.fromXml(xmlData);
			if("text".equalsIgnoreCase(msg.getMsgType())) {
				handleTextMsg(appId, msg);
			} else if ("event".equals(msg.getMsgType())){
				handleEventMsg(appId, msg);
			} 
		} catch (JAXBException e) {
			log.warn("解析公众号消息失败", e);
		}
	}

	/**
	 * 处理测试公众号推送消息
	 */
	@Async
	public void handleTestMsg(String appId, AppMsg msg) {
		String prefix = "QUERY_AUTH_CODE:";
		String text = msg.getContent();
		if(text.startsWith(prefix)) {
			String authCode = text.substring(prefix.length());
			String compId = config.getComponent().getAppId();
			String res = this.componentAccessToken()
				.flatMap(token->{
					return this.api.apiQueryAuth(compId, token, authCode);
				}).flatMap(authInfo -> {
					String access_token = authInfo.getAuthorizer_access_token();
					String toUser = msg.getFromUserName();
					String content = authCode + "_from_api";
					return this.api.sendCustomMessage(access_token, toUser, content);
				}).block();
			log.info("handleTestMsg result: {}", res);
		}
	}
	
	/**
	 * 处理公众号文本消息
	 */
	private void handleTextMsg(String appId, AppMsg msg) {
		log.debug("receive app[{}] text msg: {}", appId, msg.getContent());
	}
	
	/**
	 * 处理公众号事件消息
	 */
	private void handleEventMsg(String appId, AppMsg msg) {
		log.debug("receive app[{}] event msg: {} - {}", appId, msg.getEvent(), msg.getEventKey());
	}

	/**
	 * 解密消息
	 * @param xmlData
	 * @return
	 */
	public String decryptMsg(String xmlData) {
		log.debug("decrypt xml message: \n{}", xmlData);
		try {
			EncryptMsg msg = EncryptMsg.fromXml(xmlData);
			if(msg.getEncrypt() != null) {
				AppInfo appInfo = config.getComponent();
				String appId = appInfo.getAppId();
				String key = appInfo.getKey();
				String token = appInfo.getToken();
				WXBizMsgCrypt pc = new WXBizMsgCrypt(token, key, appId);
				String text = pc.decrypt(msg.getEncrypt());
				log.debug("decrypt messsage result: \n{}", text);
				return text;
			}
		} catch (AesException e) {
			// TODO Auto-generated catch block
			log.warn("解密消息失败", e);
		} catch (JAXBException e) {
			log.warn("解析xml失败", e);
		}
		return xmlData;
	}


	/**
	 * 处理授权回调
	 * 
	 * @param authCode 回调参数中的认证码
	 */
	public Mono<Void> handleAuthOK(String appId, String authCode) {
		String compId = config.getComponent().getAppId();
		return this.componentAccessToken()
			.flatMap(token->{
				return this.api.apiQueryAuth(compId, token, authCode);
			}).flatMap(authInfo -> {
				String key = authKey(appId, "token");
				String val = authInfo.getAuthorizer_access_token();
				int expires = authInfo.getExpires_in() - expires_off;
				this.cache.setCache(key, val, expires);
				key = authKey(appId, "refresh");
				val = authInfo.getAuthorizer_refresh_token();
				this.cache.setCache(key, val);
				return Mono.empty();
			});
	}

	String compTokenKey() {
		return compKey(config.getComponent().getAppId(), "token");
	}

	String compTicketKey() {
		return compKey(config.getComponent().getAppId(), "ticket");
	}

	String compKey(String appId, String type) {
		return String.format("weixin:open:comp:%s:%s", appId, type);
	}

	String authKey(String appId, String type) {
		return String.format("weixin:open:auth:%s:%s", appId, type);
	}
}
