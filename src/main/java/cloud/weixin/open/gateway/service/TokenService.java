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
import cloud.weixin.open.gateway.data.EncryptMsg;
import cloud.weixin.open.gateway.data.VerifyTicket;
import gaf2.core.exception.BusinessError;
import reactor.core.publisher.Mono;

@Service
public class TokenService {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Configure config;

	private CacheService cache;

	private ComponentService service;

	@Autowired
	public TokenService(Configure config, CacheService cache, ComponentService service) {
		this.config = config;
		this.cache = cache;
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
		return this.service.componentAccessToken(appInfo.getAppId(), appInfo.getSecret(), ticket)
				.map(compAccessToken -> {
					// TODO: 保存component_access_token到cache
					String res = compAccessToken.getComponent_access_token();
					int expire_in = compAccessToken.getExpires_in();
					this.cache.setCache(compTokenKey(), res, expire_in - 300);
					return res;
				});
	}

	/**
	 * 获得预授权码
	 * 
	 * @param appId 要授权的公众号appId
	 * @return
	 */
	public Mono<String> preAuthCode(String appId) {
		return this.componentAccessToken().flatMap(token -> {
			return this.service.preAuthCode(appId, token).map(res -> res.getPre_auth_code());
		});
	}

	/**
	 * 处理授权推送消息
	 * 
	 * @param xmlData
	 */
	@Async
	public void handleAuthMsg(String xmlData) {
		log.debug("receive component_verify_ticket: \n{}", xmlData);
		try {
			EncryptMsg msg = EncryptMsg.fromXml(xmlData);
			AppInfo appInfo = config.getComponent();
			String appId = msg.getAppId();
			if (!appId.equals(appInfo.getAppId())) {
				log.error("appId与配置信息不匹配: {} <-> {}", appId, appInfo.getAppId());
				return;
			}
			String key = appInfo.getKey();
			String token = appInfo.getToken();
			WXBizMsgCrypt pc = new WXBizMsgCrypt(token, key, appId);
			String text = pc.decrypt(msg.getEncrypt());
			log.debug("decrypt component_verify_ticket result: \n{}", text);
			VerifyTicket ticket = VerifyTicket.fromXml(text);
			log.debug("save component_verify_ticket: {} - {}", appId, ticket.getTicket());
			this.cache.setCache(compTicketKey(), ticket.getTicket());
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

}
