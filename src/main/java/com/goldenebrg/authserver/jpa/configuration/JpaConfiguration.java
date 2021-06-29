package com.goldenebrg.authserver.jpa.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Alex Goldenberg
 * JPA Configuration Component
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.goldenebrg.authserver.jpa.dao")
@EnableTransactionManagement
public class JpaConfiguration {

}
