package com.techbrain.chat.cofig;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.techbrain.chat.repository", transactionManagerRef = "platformTransactionManager")
public class ChatDBConfiguration {

    private final DBConfig dbConfig;

    public ChatDBConfiguration(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Bean
    JpaVendorAdapter jpaVendorAdapter() {
        System.out.println("AuthDBConfiguration ===> jpaVendorAdapter");
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    PlatformTransactionManager platformTransactionManager(EntityManagerFactory emf) {
        System.out.println("AuthDBConfiguration ===> platformTransactionManager");
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(emf);
        return txManager;
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        System.out.println("AuthDBConfiguration ===> LocalContainerEntityManagerFactoryBean");
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dbConfig.dataSource());
        factoryBean.setPackagesToScan("com.techbrain.chat.entity");
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter());
        factoryBean.setJpaProperties(jpaProperties());
        return factoryBean;
    }

    private Properties jpaProperties() {
        System.out.println("AuthDBConfiguration ===> jpaProperties");
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "com.techbrain.chat.cofig.DialectConfig");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.hbm2ddl.auto", "update"); // CRITICAL: This creates tables!
        return properties;
    }


}
