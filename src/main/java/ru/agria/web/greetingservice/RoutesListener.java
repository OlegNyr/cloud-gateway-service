package ru.agria.web.greetingservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class RoutesListener {

    private final RouteLocator routeLocator;

    private final DiscoveryClient discoveryClient;

    private Log log = LogFactory.getLog(getClass());

    public RoutesListener(DiscoveryClient dc, RouteLocator rl) {
        this.routeLocator = rl;
        this.discoveryClient = dc;
    }

    @EventListener(HeartbeatEvent.class)
    public void onHeartbeatEvent(HeartbeatEvent event) {
        if (log.isDebugEnabled()) {
            this.log.debug("onHeartbeatEvent()");
            this.discoveryClient.getServices().stream()
                    .map(x -> " " + x)
                    .forEach(this.log::debug);
        }
    }

    @EventListener(RoutesRefreshedEvent.class)
    public void onRoutesRefreshedEvent(RoutesRefreshedEvent event) {
        this.log.info("onRoutesRefreshedEvent()");
        this.routeLocator.getRoutes().stream()
                .map(x -> " " + x)
                .forEach(this.log::info);
    }
}
