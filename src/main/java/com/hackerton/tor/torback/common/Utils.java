package com.hackerton.tor.torback.common;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.hackerton.tor.torback.config.DataSourceConfiguration;

public class Utils {

//    public static MariaDbDataSource getMariaDbDataSource(){
//        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(DataSourceConfiguration.class);
//        MariaDbDataSource dataSource = (MariaDbDataSource) applicationContext.getBean("mariadbDataSource");
//        return dataSource;
//    }
    public static MysqlDataSource getMySQLDataSource(){
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(DataSourceConfiguration.class);
        MysqlDataSource dataSource = (MysqlDataSource) applicationContext.getBean("mysqlDataSource");
        return dataSource;
    }
}
