package com.example.dynamicdestinationthreadingbug;

import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantAwareRouterFactory {
    private final BinderAwareChannelResolver binderAwareChannelResolver;


    public TenantAwareRouterFactory(BinderAwareChannelResolver binderAwareChannelResolver) {
        this.binderAwareChannelResolver = binderAwareChannelResolver;
    }

    public TenantAwareRouter forChannelName(String channel) {
        TenantAwareRouter tenantAwareRouter = new TenantAwareRouter(channel);
        tenantAwareRouter.setChannelResolver(binderAwareChannelResolver);

        return tenantAwareRouter;
    }
}
