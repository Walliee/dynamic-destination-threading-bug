package com.example.dynamicdestinationthreadingbug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.messaging.MessageChannel;

@SpringBootApplication
@EnableBinding
public class DynamicDestinationThreadingBugApplication {

	public static void main(String[] args) {
		SpringApplication.run(DynamicDestinationThreadingBugApplication.class, args);
	}

	@Bean
	@ServiceActivator(inputChannel = Source.OUTPUT)
	public AbstractMappingMessageRouter messageRouter(TenantAwareRouterFactory tenantAwareRouterFactory) {
		return tenantAwareRouterFactory.forChannelName(Source.OUTPUT);
	}

	@Bean(Source.OUTPUT)
	public MessageChannel outputChannel() {
		return new DirectChannel();
	}
}

