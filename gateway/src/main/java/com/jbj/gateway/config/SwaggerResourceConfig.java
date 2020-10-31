package com.jbj.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by qingm.
 *
 * @author qingm
 * @date 2020-09-07
 * @description
 */
@Component
@Primary
public class SwaggerResourceConfig implements SwaggerResourcesProvider {

    public static final String API_URI = "/v2/api-docs";

    private RouteLocator routeLocator;

    @Autowired
    public SwaggerResourceConfig(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routeHosts = new ArrayList<>();
        this.routeLocator.getRoutes().filter(route -> route.getUri().getHost() != null)
                .subscribe(route -> routeHosts.add(route.getUri().getHost()));
        Set<String> dealed = new HashSet<>();
        routeHosts.forEach(instance -> {
            String url = "/" + instance + API_URI;
            if(!instance.equals("consul") && !instance.equals("gateway")){
                if (dealed.add(url)) {
                    SwaggerResource swaggerResource = new SwaggerResource();
                    swaggerResource.setUrl(url);
                    swaggerResource.setName(instance);
                    resources.add(swaggerResource);
                }
            }
        });
        return resources;
    }
}
