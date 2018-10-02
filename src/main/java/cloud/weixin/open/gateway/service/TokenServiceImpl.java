package cloud.weixin.open.gateway.service;

import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import cloud.weixin.open.gateway.Configure;
import cloud.weixin.open.gateway.data.AccessToken;
import cloud.weixin.open.gateway.data.Auth2Token;
import cloud.weixin.open.gateway.data.JsapiTicket;
import io.netty.util.internal.StringUtil;

@Service
public class TokenServiceImpl implements TokenService{

	private Log log = LogFactory.getLog(getClass());
	
    //@Autowired
    private Configure config;

    private CacheService cache;

    @Autowired
    public TokenServiceImpl(Configure config, CacheServiceImpl cache){
    	this.config = config;
    	this.cache = cache;
    }

    @Override
	public String fetchToken(String appid, String orginfo, boolean force){
//    	String val = config.getSecret().get(appid);
//    	if(val == null)
//			val = appid + "==";
//    	return val;
    	
    	//Assert.notNull(appid, "appid is null");
    	Assert.isTrue(appid != null || orginfo != null, "appid is null");

    	if(appid == null)
    		appid = config.getAppInfo().get(orginfo);
    	else{
    		if(!config.getSecret().containsKey(appid) && config.getAppInfo().containsKey(appid)){
    			appid = config.getAppInfo().get(appid);
    		}
    	}

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
    	
    	String secret = config.getSecret().get(appid);
    	Assert.notNull(secret, "can't find secret configure for appid:" + appid);
    	
    	AccessToken token = getAccessToken(appid, secret);
    	if(token != null && StringUtil.isNullOrEmpty(token.getErrmsg())){
        	setToken(appid, token.getAccess_token(), token.getExpires_in() - 300);
        	return token.getAccess_token();
    	}
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
		for(Entry<String, String> entry : config.getAppInfo().entrySet()){
			if(entry.getValue().equals(appid)){
				appid = entry.getKey(); 
				break;
			}
		}
		return appid + (ticket?"ticket":"");
	}
	
	@Override
	public String fetchTicket(String appid, String orginfo) {
    	Assert.isTrue(appid != null || orginfo != null, "appid is null");

    	if(appid == null)
    		appid = config.getAppInfo().get(orginfo);
    	
    	//TODO: 针对吉林移动交接，所有使用吉林移动的jssdk调用，切换为和生活的. dyg@2017.8.28
    	if("wxb4276a822d76aca1".equals(appid))
    		appid = "wx1be04c15feb25455";

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
	

    @Override
	public String fetchOpenid(String appid, String code){
    	Assert.notNull(appid, "appid is null");
    	Assert.notNull(code, "code is null");

    	String secret = config.getSecret().get(appid);
    	Assert.notNull(secret, "can't find secret configure for appid:" + appid);
    	
    	Auth2Token token = getAuth2Token(appid, secret, code);
    	if(token != null && StringUtil.isNullOrEmpty(token.getErrmsg())){
    		if(token.getErrcode() != 0){
    			log.error(String.format("fetch openid by code fail: %d - %s", token.getErrcode(), token.getErrmsg()));
    			return null;
    		}
        	return token.getOpenid();
    	}

    	return null;
	}
    
    private AccessToken getAccessToken(String appid, String secret) {
    	return null;
    }
    
	private JsapiTicket getJsapiTicket(String token) {
		return null;
	}
	
	private Auth2Token getAuth2Token(String appid, String secret, String code) {
		return null;
	}
}
