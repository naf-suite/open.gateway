package cloud.weixin.open.gateway.service;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.fastjson.JSONObject;

import cloud.weixin.open.gateway.Configure;
import cloud.weixin.open.gateway.aes.AesException;
import cloud.weixin.open.gateway.aes.WXBizMsgCrypt;
import cloud.weixin.open.gateway.data.Auth2Token;
import cloud.weixin.open.gateway.data.ComponentAccessToken;
import cloud.weixin.open.gateway.data.EncryptMsg;
import cloud.weixin.open.gateway.data.JsapiTicket;
import cloud.weixin.open.gateway.data.PreAuthCode;
import cloud.weixin.open.gateway.data.QueryAuthRes;
import cloud.weixin.open.gateway.data.QueryAuthRes.Authorization_info;
import cloud.weixin.open.gateway.data.VerifyTicket;
import gaf2.core.exception.BusinessError;
import reactor.core.publisher.Mono;

@Service
public class TokenService {

	private Logger log = LoggerFactory.getLogger(getClass());
	
    //@Autowired
    private Configure config;

    private CacheService cache;

    @Autowired
    public TokenService(Configure config, CacheService cache){
    	this.config = config;
    	this.cache = cache;
    }

	public String fetchToken(String appid, String orginfo, boolean force){
//    	String val = config.getSecret().get(appid);
//    	if(val == null)
//			val = appid + "==";
//    	return val;
    	
    	//Assert.notNull(appid, "appid is null");
    	Assert.isTrue(appid != null || orginfo != null, "appid is null");

//    	if(appid == null)
//    		appid = config.getAppInfo().get(orginfo);
//    	else{
//    		if(!config.getSecret().containsKey(appid) && config.getAppInfo().containsKey(appid)){
//    			appid = config.getAppInfo().get(appid);
//    		}
//    	}

    	String val;
    	if(!force){
        	val = getToken(appid);
        	if(val != null)
        		return val;
    	}
    	
    	//TODO: 重新获取Token
    	val = reqToken(appid);
    	
    	return val;
	}
    
    String reqToken(String appid){
    	log.info("request token for: " + appid);
    	
//    	String secret = config.getSecret().get(appid);
//    	Assert.notNull(secret, "can't find secret configure for appid:" + appid);
//    	
//    	AccessToken token = getAccessToken(appid, secret);
//    	if(token != null && StringUtil.isNullOrEmpty(token.getErrmsg())){
//        	setToken(appid, token.getAccess_token(), token.getExpires_in() - 300);
//        	return token.getAccess_token();
//    	}
    	return null;
    }
    
	void setToken(String appid,String token,long expire) {
		String key = genkey(appid);
		this.cache.setCache(key, token, expire, !config.isNewMode());
	}

	String getToken(String appid) {
		String key = genkey(appid);
		return this.cache.getCache(key);
	}
	
	String genkey(String appid){
		return genkey(appid,false);
	}
	String genkey(String appid, boolean ticket){
		if(config.isNewMode()){
			return (ticket?"wxticket:":"wxtoken:") + appid;
		}
		//convert appid to orginfo
//		for(Entry<String, String> entry : config.getAppInfo().entrySet()){
//			if(entry.getValue().equals(appid)){
//				appid = entry.getKey(); 
//				break;
//			}
//		}
		return appid + (ticket?"ticket":"");
	}
	
	public String fetchTicket(String appid, String orginfo) {
    	Assert.isTrue(appid != null || orginfo != null, "appid is null");

//    	if(appid == null)
//    		appid = config.getAppInfo().get(orginfo);
//    	
//    	//TODO: 针对吉林移动交接，所有使用吉林移动的jssdk调用，切换为和生活的. dyg@2017.8.28
//    	if("wxb4276a822d76aca1".equals(appid))
//    		appid = "wx1be04c15feb25455";

    	String val = getTicket(appid);
    	if(val != null)
    		return val;
    	
    	//TODO: 重新获取Token
    	val = reqTicket(appid);
    	
    	return val;
	}
    
    String reqTicket(String appid){
    	log.info("request ticket for: " + appid);
    	
    	String token = fetchToken(appid,null,false);
    	Assert.notNull(token,"fetch access_token fail!");
    	
    	JsapiTicket ticket = getJsapiTicket(token);
    	if(ticket!= null){
        	setTicket(appid, ticket.getTicket(), ticket.getExpires_in() - 300);
        	return ticket.getTicket();
    	}
    	return null;
    }
	void setTicket(String appid,String ticket,long expire) {
		String key = genkey(appid, true);
		this.cache.setCache(key, ticket, expire, !config.isNewMode());
	}

	String getTicket(String appid) {
		String key = genkey(appid,true);
		return this.cache.getCache(key);
	}
	
	public String fetchOpenid(String appid, String code){
    	Assert.notNull(appid, "appid is null");
    	Assert.notNull(code, "code is null");

//    	String secret = config.getSecret().get(appid);
//    	Assert.notNull(secret, "can't find secret configure for appid:" + appid);
//    	
//    	Auth2Token token = getAuth2Token(appid, secret, code);
//    	if(token != null && StringUtil.isNullOrEmpty(token.getErrmsg())){
//    		if(token.getErrcode() != 0){
//    			log.error(String.format("fetch openid by code fail: %d - %s", token.getErrcode(), token.getErrmsg()));
//    			return null;
//    		}
//        	return token.getOpenid();
//    	}

    	return null;
	}
    
	public void saveVerifyTicket(String xmlData) {
		try {
			log.debug("receive component_verify_ticket: \n{}", xmlData);
			EncryptMsg msg = EncryptMsg.fromXml(xmlData);
			String appId = msg.getAppId();
			String key = config.getAppKey(appId);
			String token = config.getAppKey(appId);
			WXBizMsgCrypt pc = new WXBizMsgCrypt(token, key, appId);
			String text = pc.decrypt(msg.getEncrypt());
			log.debug("decrypt component_verify_ticket result: \n{}", text);
			VerifyTicket ticket = VerifyTicket.fromXml(text);
			log.debug("save component_verify_ticket: {} - {}", appId, ticket.getTicket());
			this.cache.setCache(verifyTicketKey(appId), ticket.getTicket());
		} catch (AesException e) {
			// TODO Auto-generated catch block
			log.warn("解密component_verify_ticket失败", e);
		} catch (JAXBException e) {
			log.warn("解析component_verify_ticket失败", e);
		}
			
	}

	public String fetchVerifyTicket(String appid) {
		return this.cache.getCache(verifyTicketKey(appid));
	}

	String verifyTicketKey(String appid){
		return "weixin:open:" + appid + ":verify_ticket";
	}


	JsapiTicket getJsapiTicket(String token) {
		return null;
	}
	
	Auth2Token getAuth2Token(String appid, String secret, String code) {
		return null;
	}

}
