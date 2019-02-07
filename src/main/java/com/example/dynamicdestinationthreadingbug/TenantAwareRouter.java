package com.example.dynamicdestinationthreadingbug;

import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.messaging.Message;

import java.util.Collections;
import java.util.List;

public class TenantAwareRouter extends AbstractMappingMessageRouter {
    public static final String TENANT_ID = "x-tenant-id";

    private final String topicName;

    public TenantAwareRouter(String topicName) {
        this.topicName = topicName;
    }


    @Override
    protected List<Object> getChannelKeys(Message<?> message) {
        String tenantId = message.getHeaders().get(TENANT_ID, String.class);

        return Collections.singletonList(String.format("%s_%s", tenantId, topicName));
    }
}
