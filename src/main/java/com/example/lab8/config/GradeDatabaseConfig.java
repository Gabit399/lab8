package com.example.lab8.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * grade_db: DataSource + EntityManagerFactory + TransactionManager.
 * Only repositories in com.example.lab8.grade.repository use this database.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.lab8.grade.repository",
        entityManagerFactoryRef = "gradeEntityManagerFactory",
        transactionManagerRef = "gradeTransactionManager"
)
public class GradeDatabaseConfig {

    @Bean
    public DataSource gradeDataSource(Environment env) {
        return DatabaseConfigSupport.dataSource(env, "grade");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean gradeEntityManagerFactory(
            @Qualifier("gradeDataSource") DataSource dataSource, Environment env) {
        return DatabaseConfigSupport.entityManagerFactory(
                dataSource, env, "grade", "com.example.lab8.grade.entity");
    }

    @Bean
    public PlatformTransactionManager gradeTransactionManager(
            @Qualifier("gradeEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
