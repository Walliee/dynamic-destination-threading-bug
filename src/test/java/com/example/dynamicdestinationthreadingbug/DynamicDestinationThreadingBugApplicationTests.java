package com.example.dynamicdestinationthreadingbug;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		DynamicDestinationThreadingBugApplication.class,
		DynamicDestinationThreadingBugApplicationTests.TestConfig.class
})
public class DynamicDestinationThreadingBugApplicationTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDestinationThreadingBugApplicationTests.class);

	@TestConfiguration
	static class TestConfig {

		@Bean
		public CountDownLatch latch() {
			return new CountDownLatch(1);
		}

		@Bean
		public DynamicDestinationConfigurationCallback dynamicDestinationConfigurationCallback(
				CountDownLatch latch
		) {
			return new DynamicDestinationConfigurationCallback(latch);
		}
	}

	@Autowired
	@Qualifier(Source.OUTPUT)
	MessageChannel outChannel;

	@Autowired
	CountDownLatch latch;

	Executor executor = Executors.newFixedThreadPool(100);

	@Test
	public void testBinderAwareChannelResolver() {
		AtomicReference<MessageDeliveryException> exception = new AtomicReference<>();
		List<CompletableFuture<Void>> futures = IntStream.range(1, 101)
				.mapToObj(i -> CompletableFuture.runAsync(() -> {
					Message<String> message = MessageBuilder.withPayload("payload" + i)
							.setHeader(TenantAwareRouter.TENANT_ID, "tenantId")
							.build();

					if (i == 75) {
						latch.countDown();
					}

					try {
						LOGGER.info("Sending message {}", i);
						outChannel.send(message);
					} catch (MessageDeliveryException e) {
						LOGGER.error("Failed to send message", e);
						exception.set(e);
					}
				}, executor))
				.collect(Collectors.toList());

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
				.join();

		Assert.assertEquals(MessageDeliveryException.class, exception.get().getClass());
		Assert.assertTrue(exception.get().getMessage()
				.startsWith("Dispatcher has no subscribers for channel 'application.tenantId_output'"));
	}

}

