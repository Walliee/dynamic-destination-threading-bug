package com.example.dynamicdestinationthreadingbug;

import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaProducerProperties;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.messaging.MessageChannel;

import java.util.concurrent.CountDownLatch;

public class DynamicDestinationConfigurationCallback implements BinderAwareChannelResolver.NewDestinationBindingCallback<KafkaProducerProperties> {
    private final CountDownLatch latch;

    public DynamicDestinationConfigurationCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void configure(String channelName, MessageChannel channel, ProducerProperties producerProperties, KafkaProducerProperties extendedProducerProperties) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
