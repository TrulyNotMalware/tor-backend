package com.hackerton.tor.torback.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

@Configuration
@AllArgsConstructor
@PropertySource("application.properties")
public class DatabaseConfiguration extends AbstractR2dbcConfiguration {

    private Environment environment;

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        String driverName = "r2dbc:mysql://";
        String url = driverName + environment.getProperty("spring.r2dbc.username")+":"
                + environment.getProperty("spring.r2dbc.password") + "@"
                + environment.getProperty("tor.database.svc.name") + ":"
                + environment.getProperty("tor.database.svc.port") + "/"
                + environment.getProperty("tor.database.table.name");
        ConnectionFactory connectionFactory = ConnectionFactories.get(url);
        return connectionFactory;
    }

}