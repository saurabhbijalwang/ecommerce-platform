package com.ecommerce.platform.gateway;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class ApiGatewayApplicationTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void routesKnownApisToTheirLoadBalancedServiceIds() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(5));

        Map<String, String> destinations = routes.stream()
                .collect(Collectors.toMap(
                        RouteDefinition::getId,
                        route -> route.getUri().toString()));

        assertThat(destinations).containsExactlyInAnyOrderEntriesOf(Map.of(
                "auth-service", "lb://auth-service",
                "product-search-service", "lb://search-service",
                "cms-service", "lb://cms-service",
                "order-service", "lb://order-service"));
    }
}
