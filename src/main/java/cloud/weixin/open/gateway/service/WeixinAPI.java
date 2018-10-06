package cloud.weixin.open.gateway.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.fastjson.JSONObject;

import cloud.weixin.open.gateway.data.AuthorizerAccessToken;
import cloud.weixin.open.gateway.data.ComponentAccessToken;
import cloud.weixin.open.gateway.data.PreAuthCode;
import cloud.weixin.open.gateway.data.QueryAuthRes;
import cloud.weixin.open.gateway.data.QueryAuthRes.Authorization_info;
import cloud.weixin.open.gateway.data.WeixinMsg;
import gaf2.core.exception.BusinessError;
import reactor.core.publisher.Mono;

@Service
public class WeixinAPI {
	private Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * 2、获取第三方平台component_access_token
	 * @param appId
	 * @param secret
	 * @param ticket
	 * @return
	 */
	public Mono<ComponentAccessToken> componentAccessToken(String compId, String secret, String ticket) {
	    WebClient client = WebClient.create("https://api.weixin.qq.com/cgi-bin/component");

	    String req = new JSONObject()
	    		.fluentPut("component_appid", compId)
	    		.fluentPut("component_appsecret", secret)
	    		.fluentPut("component_verify_ticket", ticket)
	    		.toJSONString();
	    
	    Mono<ComponentAccessToken> result = client.post()
	            .uri("/api_component_token")
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON)
	            .syncBody(req)
	            .retrieve()
	            .bodyToMono(String.class)
	            .map(msg->{
	            	log.debug("api_component_token return: {}", msg);
	            	ComponentAccessToken res = JSONObject.parseObject(msg, ComponentAccessToken.class);
	            	if(res.getErrcode() == 0) return res;
	            	log.error("api_component_token fail: {} - {}", res.getErrcode(), res.getErrmsg());
	            	throw new BusinessError(BusinessError.ERR_SERVICE_FAULT, "获取第三方平台调用凭据失败");
	            });
	    
	    return result;
    }
    
	/**
	 * 3、获取预授权码pre_auth_code
	 * @param appId
	 * @param token
	 * @return
	 */
	public Mono<PreAuthCode> preAuthCode(String compId, String token) {

    	log.debug("call preAuthCode, compId: {} token: {}", compId, token);
    	
    	WebClient client = WebClient.create("https://api.weixin.qq.com/cgi-bin/component");

	    String req = new JSONObject()
	    		.fluentPut("component_appid", compId)
	    		.toJSONString();
	    
	    Mono<PreAuthCode> result = client.post()
	            .uri("/api_create_preauthcode?component_access_token=" + token)
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON)
	            .syncBody(req)
	            .retrieve()
	            .bodyToMono(String.class)
	            .map(msg->{
	            	log.debug("api_create_preauthcode return: {}", msg);
	            	PreAuthCode res = JSONObject.parseObject(msg, PreAuthCode.class);
	            	if(res.getErrcode() == 0) return res;
	            	log.error("api_create_preauthcode fail: {} - {}", res.getErrcode(), res.getErrmsg());
	            	throw new BusinessError(BusinessError.ERR_SERVICE_FAULT, "获取预授权码失败");
	            });
	    
	    return result;
    }

	/**
	 * 4、使用授权码换取公众号或小程序的接口调用凭据和授权信息
	 * @param appId
	 * @param token
	 * @param code
	 * @return
	 */
	public Mono<Authorization_info> apiQueryAuth(String compId, String token, String code) {
	    WebClient client = WebClient.create("https://api.weixin.qq.com/cgi-bin/component");

	    String req = new JSONObject()
	    		.fluentPut("component_appid", compId)
	    		.fluentPut("authorization_code", code)
	    		.toJSONString();
	    
	    Mono<Authorization_info> result = client.post()
	            .uri("/api_query_auth?component_access_token=" + token)
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON)
	            .syncBody(req)
	            .retrieve()
	            .bodyToMono(String.class)
	            .map(msg->{
	            	log.debug("api_query_auth return: {}", msg);
	            	QueryAuthRes res = JSONObject.parseObject(msg, QueryAuthRes.class);
	            	if(res.getErrcode() == 0) return res.getAuthorization_info();
	            	log.error("api_query_auth fail: {} - {}", res.getErrcode(), res.getErrmsg());
	            	throw new BusinessError(BusinessError.ERR_SERVICE_FAULT, "换取调用凭据失败");
	            });
	    
	    return result;
    }

	/**
	 * 5、获取（刷新）授权公众号或小程序的接口调用凭据（令牌）
	 * @param appId
	 * @param token
	 * @param authApp
	 * @param refreshToken
	 * @return
	 */
	public Mono<AuthorizerAccessToken> apiAuthorizerToken(String compId, String token, String appId, String refreshToken) {
	    WebClient client = WebClient.create("https://api.weixin.qq.com/cgi-bin/component");

	    String req = new JSONObject()
	    		.fluentPut("component_appid", compId)
	    		.fluentPut("authorizer_appid", appId)
	    		.fluentPut("authorizer_refresh_token", refreshToken)
	    		.toJSONString();
	    
	    Mono<AuthorizerAccessToken> result = client.post()
	            .uri("/api_authorizer_token?component_access_token=" + token)
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON)
	            .syncBody(req)
	            .retrieve()
	            .bodyToMono(String.class)
	            .map(msg->{
	            	log.debug("api_authorizer_token return: {}", msg);
	            	AuthorizerAccessToken res = JSONObject.parseObject(msg, AuthorizerAccessToken.class);
	            	if(res.getErrcode() == 0) return res;
	            	log.error("api_authorizer_token fail: {} - {}", res.getErrcode(), res.getErrmsg());
	            	throw new BusinessError(BusinessError.ERR_SERVICE_FAULT, "刷新调用凭据失败");
	            });
	    
	    return result;
    }

	/**
	 * 客服接口-发消息
	 */
	public Mono<String> sendCustomMessage(String token, String toUser, String content) {
    	log.debug("call sendCustomMessage, token: {} toUser: {} content: {}", token, toUser, content);

    	String req = new JSONObject()
	    		.fluentPut("touser", toUser)
	    		.fluentPut("msgtype", "text")
	    		.fluentPut("text", new JSONObject().fluentPut("content", content))
	    		.toJSONString();
	    
	    log.debug("sendCustomMessage request data: {}", req);

	    Mono<String> result = apiPost("/message/custom/send", token, req);
	    
	    return result;
    }

	/**
	 * Post接口
	 */
	public Mono<String> apiPost(String api, String token, String postData) {
		return apiPost(api, token, postData, true);
	}
	public Mono<String> apiPost(String api, String token, String postData, boolean checkRes) {
		Assert.hasText(api, "api 不能为空");
		Assert.hasText(token, "token 不能为空");
    	log.info("api post {}, token: {} ", api, token);
    	log.debug("{} postData: {}", api, postData);
    	
    	WebClient client = WebClient.create("https://api.weixin.qq.com/cgi-bin");

	    String req = postData == null?"":postData;
	    
	    String uri;
	    try {
			uri = api + "?access_token=" + URLEncoder.encode(token, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.warn("URL编码错误", e);
			uri = api + "?access_token=" + token;
		}

	    Mono<String> result = client.post()
	            .uri(uri)
	            .contentType(MediaType.APPLICATION_JSON)
	            .accept(MediaType.APPLICATION_JSON)
	            .syncBody(req)
	            .retrieve()
	            .bodyToMono(String.class)
	            .map(msg->{
	            	log.debug("post {} return: {}", api, msg);
	            	if(checkRes) {
		            	WeixinMsg res = JSONObject.parseObject(msg, WeixinMsg.class);
		            	if(res.getErrcode() != 0) {
			            	log.error("post {} fail: {} - {}", api, res.getErrcode(), res.getErrmsg());
			            	throw new BusinessError(BusinessError.ERR_SERVICE_FAULT, "调用接口失败");
		            	}
	            	}
	            	return msg;
	            });
	    
	    return result;
    }
	public Mono<String> apiGet(String api, String token) {
		return apiGet(api, token, true);
	}
	public Mono<String> apiGet(String api, String token, boolean checkRes) {
		Assert.hasText(api, "api 不能为空");
		Assert.hasText(token, "token 不能为空");
    	log.info("api get {}, token: {} ", api, token);
    	
    	WebClient client = WebClient.create("https://api.weixin.qq.com/cgi-bin");

	    String uri;
	    try {
			uri = api + "?access_token=" + URLEncoder.encode(token, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.warn("URL编码错误", e);
			uri = api + "?access_token=" + token;
		}

	    Mono<String> result = client.get()
	            .uri(uri)
	            .accept(MediaType.APPLICATION_JSON)
	            .retrieve()
	            .bodyToMono(String.class)
	            .map(msg->{
	            	log.debug("post {} return: {}", api, msg);
	            	if(checkRes) {
		            	WeixinMsg res = JSONObject.parseObject(msg, WeixinMsg.class);
		            	if(res.getErrcode() != 0) {
			            	log.error("get {} fail: {} - {}", api, res.getErrcode(), res.getErrmsg());
			            	throw new BusinessError(BusinessError.ERR_SERVICE_FAULT, "调用接口失败");
		            	}
	            	}
	            	return msg;
	            });
	    
	    return result;
    }
}
