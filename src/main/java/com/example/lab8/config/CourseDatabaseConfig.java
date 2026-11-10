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
 * course_db: DataSource + EntityManagerFactory + TransactionManager.
 * Only repositories in com.example.lab8.course.repository use this database.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.lab8.course.repository",
        entityManagerFactoryRef = "courseEntityManagerFactory",
        transactionManagerRef = "courseTransactionManager"
)
public class CourseDatabaseConfig {

    @Bean
    public DataSource courseDataSource(Environment env) {
        return DatabaseConfigSupport.dataSource(env, "course");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean courseEntityManagerFactory(
            @Qualifier("courseDataSource") DataSource dataSource, Environment env) {
        return DatabaseConfigSupport.entityManagerFactory(
                dataSource, env, "course", "com.example.lab8.course.entity");
    }

    @Bean
    public PlatformTransactionManager courseTransactionManager(
            @Qualifier("courseEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
