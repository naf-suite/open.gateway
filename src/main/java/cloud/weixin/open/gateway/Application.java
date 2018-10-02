package cloud.weixin.open.gateway;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cloud.weixin.open.gateway.data.EncryptMsg;
import cloud.weixin.open.gateway.service.TokenService;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Controller
@RequestMapping(path = { "/open" })
@EnableConfigurationProperties({ Configure.class })
public class Application {
	
	Logger log =  LoggerFactory.getLogger(Application.class);
	
	@Autowired
	TokenService service;

	@GetMapping("/demo")
	public Mono<String> demo() {
		return Mono.just("demo");
	}

	@RequestMapping("/{corpid}/{agentid}/demo2")
	public @ResponseBody Mono<String> demo2(@PathVariable(name = "corpid", required = true) String corpid) {
		return Mono.just("hello," + corpid);
	}
	
	@PostMapping("/auth_msg")
	public @ResponseBody Mono<String> auth_msg(@RequestBody String xmlData) {
		log.info("request auth_msg...");
		service.saveVerifyTicket(xmlData);

		return Mono.just("success");
	}
	

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}