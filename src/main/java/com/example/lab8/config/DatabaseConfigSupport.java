package com.example.lab8.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared helpers for the three database configurations.
 * Each database reads its own "app.datasource.<name>.*" properties.
 */
final class DatabaseConfigSupport {

    private DatabaseConfigSupport() {
    }

    static DataSource dataSource(Environment env, String name) {
        String prefix = "app.datasource." + name + ".";
        HikariDataSource ds = new HikariDataSource();
        ds.setPoolName(name + "-pool");
        ds.setJdbcUrl(env.getRequiredProperty(prefix + "url"));
        ds.setUsername(env.getRequiredProperty(prefix + "username"));
        ds.setPassword(env.getProperty(prefix + "password", ""));
        ds.setDriverClassName(env.getProperty(prefix + "driver-class-name", "org.postgresql.Driver"));
        return ds;
    }

    static LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, Environment env, String name, String entityPackage) {

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan(entityPackage);
        emf.setPersistenceUnitName(name);
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", env.getProperty("app.jpa.ddl-auto", "update"));
        props.put("hibernate.show_sql", env.getProperty("app.jpa.show-sql", "false"));
        props.put("hibernate.format_sql", "true");
        emf.setJpaPropertyMap(props);
        return emf;
    }
}
