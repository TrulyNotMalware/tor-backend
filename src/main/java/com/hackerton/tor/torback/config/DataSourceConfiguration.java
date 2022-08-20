package com.hackerton.tor.torback.config;

import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;


@ComponentScan
@Configuration
@AllArgsConstructor
@PropertySource("application.properties")
public class DataSourceConfiguration {

    private Environment environment;

//    @Bean(name = "mariadbDataSource")
//    public MariaDbDataSource getDataSource() throws SQLException {
//        MariaDbDataSource dataSource = new MariaDbDataSource();
//        dataSource.setUrl(environment.getProperty("spring.datasource.url"));
//        dataSource.setUser(environment.getProperty("spring.r2dbc.username"));
//        dataSource.setPassword(environment.getProperty("spring.r2dbc.password"));
//        return dataSource;
//    }

    @Bean(name = "mysqlDataSource")
    public MysqlDataSource getMySQLDataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl(environment.getProperty("spring.datasource.url"));
        dataSource.setUser(environment.getProperty("spring.r2dbc.username"));
        dataSource.setPassword(environment.getProperty("spring.r2dbc.password"));
        return dataSource;
    }
}

