package cloud.weixin.open.gateway;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cloud.weixin.open.gateway.data.AppMsg;
import cloud.weixin.open.gateway.service.WeixinService;
import cloud.weixin.open.gateway.service.WeixinAPI;
import gaf2.core.exception.BusinessError;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Controller
@RequestMapping(path = { "/open" })
@EnableConfigurationProperties({ Configure.class })
@EnableAsync
public class Application {
	
	Logger log =  LoggerFactory.getLogger(Application.class);
	
	@Autowired
	WeixinService service;

	@Autowired
	WeixinAPI weixin;

    @Autowired
    private Configure config;

    @GetMapping("/demo")
	public Mono<String> demo() {
		return Mono.just("demo");
	}

	@RequestMapping("/{corpid}/{agentid}/demo2")
	public @ResponseBody Mono<String> demo2(@PathVariable(name = "corpid", required = true) String corpid) {
		return Mono.just("hello," + corpid);
	}
	
	/**
	 * 接收授权消息推送
	 * @param xmlData
	 * @return
	 */
	@PostMapping("/auth_msg")
	public @ResponseBody Mono<String> auth_msg(@RequestBody String xmlData) {
		log.info("request auth_msg...");
		service.handleAuthMsg(xmlData);
		return Mono.just("success");
	}
	
	/**
	 * 接收公众号消息推送
	 */
	@PostMapping("/{appId}/app_msg")
	public @ResponseBody Mono<String> app_msg(@PathVariable(name="appId",required=true) String appId, @RequestBody String xmlData) {
		log.info("request {} app_msg...", appId);
		service.handleAppMsg(appId, xmlData);
		return Mono.just("success");
	}
	
	/**
	 * 接收测试公众号消息推送
	 * @throws JAXBException 
	 */
	@PostMapping("/wx570bc396a51b8ff8/app_msg")
	public @ResponseBody String app_msg(@RequestBody String xmlData) throws JAXBException {
		log.info("request test app_msg...");
		String appId = "wx570bc396a51b8ff8";
		xmlData = service.decryptMsg(xmlData);
		AppMsg msg = AppMsg.fromXml(xmlData);
		if("text".equals(msg.getMsgType())) {
			if("TESTCOMPONENT_MSG_TYPE_TEXT".equalsIgnoreCase(msg.getContent())) {
				String res = String.format("<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[%s]]></FromUserName><CreateTime>%d</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[TESTCOMPONENT_MSG_TYPE_TEXT_callback]]></Content></xml>", msg.getFromUserName(), msg.getToUserName(), Calendar.getInstance().getTimeInMillis()/1000);
				log.debug("return test app_msg: {}", res);
				return res;
			}
			service.handleTestMsg(appId, msg);
		}
		return "success";
	}

	/**
	 * 公众号授权请求
	 * @param appId 公众号appId
	 * @return 重定向到授权页面
	 * @throws UnsupportedEncodingException 
	 */
	@GetMapping("/{appId}/auth")
	public Mono<String> auth(@PathVariable(name="appId",required=true) String appId, @RequestHeader("User-Agent") String userAgent, Model model) throws UnsupportedEncodingException {
		log.info("request {} auth...", appId);
		String template = null;
		if(userAgent.contains("micromessenger")) {
			// TODO: 微信打开
			template = "https://mp.weixin.qq.com/safe/bindcomponent?action=bindcomponent&no_scan=1&component_appid=%s&pre_auth_code=%s&redirect_uri=%s&auth_type=1&biz_appid=%s#wechat_redirect"; 
		} else {
			// TODO: 非微信打开，显示二维码页面
			template = "https://mp.weixin.qq.com/cgi-bin/componentloginpage?component_appid=%s&pre_auth_code=%s&redirect_uri=%s&auth_type=1&biz_appid=%s#wechat_redirect";
		}
		
		String uri_template = template;
		String baseUrl = config.getBaseUrl();
		if(baseUrl.endsWith("/")) baseUrl.substring(0, baseUrl.length()-1);
		String redirect_uri = URLEncoder.encode(String.format("%s/%s/auth_ok", config.getBaseUrl(), appId), "UTF-8");
		
		return this.service.preAuthCode().map(pre_auth_code->{
				String url = String.format(uri_template, 
						config.getComponent().getAppId(),
						pre_auth_code,
						redirect_uri,
						appId);
					
				model.addAttribute("url", url);
				return "auth";
			});

	}

	/**
	 * 授权成功回调地址
	 */
	@GetMapping("/{appId}/auth_ok")
	public Mono<String> auth_ok(@PathVariable(name="appId",required=true) String appId, String auth_code, int expires_in, Model model) {
		log.info("request {} auth_ok...", appId);
		log.debug("auth_code: {} expire_in: {}", auth_code, expires_in);
		model.addAttribute("message", "三方平台授权成功");
		return this.service.handleAuthOK(appId, auth_code)
			.thenReturn("success");
	}

	/**
	 * 微信接口代理
	 */
	@PostMapping("/api.weixin.qq.com/**")
	public @ResponseBody Mono<String> apiPost(String appid, @RequestBody String postData, ServerHttpRequest request) {
		String api = request.getPath().subPath(6).value();
		log.info("request api {} for ...", api, appid);
		log.debug("postData: {}", postData);
		return this.service.accessToken(appid)
		.flatMap(token->{
			return this.weixin.apiPost(api, token, postData, false);
		}).onErrorResume(err->{
			String res = new JSONObject()
			.fluentPut("errcode", -1)
			.fluentPut("errmsg", err.getMessage())
			.toJSONString();
			return Mono.just(res);
		});
	}

	@GetMapping("/api.weixin.qq.com/**")
	public @ResponseBody Mono<String> apiGet(String appid, ServerHttpRequest request) {
		String api = request.getPath().subPath(6).value();
		Map<String, String> query = request.getQueryParams().toSingleValueMap();
		query.remove("appId");
		log.info("request api {} for ...", api, appid);
		return this.service.accessToken(appid)
		.flatMap(token->{
			return this.weixin.apiGet(api, token, query, false);
		}).onErrorResume(err->{
			String res = new JSONObject()
			.fluentPut("errcode", -1)
			.fluentPut("errmsg", err.getMessage())
			.toJSONString();
			return Mono.just(res);
		});
	}

	Mono<String> error(String message, Model model) {
		model.addAttribute("message", message);
		return Mono.just("error");
	}

	@ExceptionHandler
	public Mono<String> handleException(Throwable ex, Model model) {
		if (ex instanceof BusinessError) {
			BusinessError err = (BusinessError) ex;
			log.warn("处理失败: {}-{}", err.getErrorCode(), err.getMessage());
			log.debug("错误详情: ", ex);
			return error(err.getMessage(), model);
		} else {
			log.error("处理失败: {}", ex.getMessage());
			log.debug("错误详情: ", ex);
			return error("请求处理失败，请稍后再试", model);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
