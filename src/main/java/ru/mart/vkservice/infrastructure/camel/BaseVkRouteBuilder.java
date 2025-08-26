package ru.mart.vkservice.infrastructure.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseVkRouteBuilder extends RouteBuilder {
    protected static final String VK_API_VERSION = "5.199";
}
