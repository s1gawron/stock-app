package com.s1gawron.stockexchange.configuration.rabbit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitProperties {

    private final String host;

    private final int port;

    private final String username;

    private final String password;

    public RabbitProperties(@Value("${spring.rabbitmq.host}") final String host, @Value("${spring.rabbitmq.port}") final int port,
        @Value("${spring.rabbitmq.username}") final String username, @Value("${spring.rabbitmq.password}") final String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}