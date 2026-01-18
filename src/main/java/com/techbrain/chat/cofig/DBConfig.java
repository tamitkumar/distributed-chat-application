package com.techbrain.chat.cofig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class DBConfig {
    @Bean
    DataSource dataSource() {
        System.out.println("In AuthDBConfig");
        // Use environment variables for Docker compatibility
        String dbUser = System.getenv().getOrDefault("DB_USER", "root");
        String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "root");
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        DriverManagerDataSource ds = new DriverManagerDataSource(getDBUrl(), dbUser, dbPassword);
        try {
            System.out.println("In AuthDBConfig ===> Set Driver Class");
            ds.setDriverClassName(driverClassName);
        } catch (Exception e) {
            System.out.println("In AuthDBConfig ===> caught exception {}");
            throw new RuntimeException(e);
        }
        try {
            System.out.println("In AuthDBConfig ==> close DB Connection");
            ds.getConnection().close();
        } catch (SQLException e) {
            System.out.println("In AuthDBConfig ===> caught exception {} while closing connection");
            throw new RuntimeException(e);
        }
        return ds;
    }

    private String getDBUrl() {
        System.out.println("In AuthDBConfig ===> getDBUrl");
        // Use environment variables for Docker compatibility
        String dbHost = System.getenv().getOrDefault("DB_HOST", "localhost");
        String dbPort = System.getenv().getOrDefault("DB_PORT", "3307");  // Changed to 3307
        String dbName = "chat";
        String dbUrlPrefix = "jdbc:mysql://";
        String params = "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return dbUrlPrefix + dbHost + ":" + dbPort + "/" + dbName + params;
    }
}
