package cloud.weixin.open.gateway.service;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig {

	@Bean
	public TopicExchange topic() {
		return new TopicExchange("weixin.event", false, false);
	}

}
