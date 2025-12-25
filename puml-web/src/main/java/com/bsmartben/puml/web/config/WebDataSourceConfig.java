package com.bsmartben.puml.web.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * WebDB DataSource Configuration (MySQL)
 * Scans web layer mappers: com.bsmartben.puml.web..mapper
 */
@Configuration
@MapperScan(
    basePackages = "com.bsmartben.puml.web..mapper",
    sqlSessionFactoryRef = "webSqlSessionFactory"
)
public class WebDataSourceConfig {

    @Bean(name = "webDataSource")
    @ConfigurationProperties(prefix = "app.datasource.web")
    public DataSource webDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "webSqlSessionFactory")
    public SqlSessionFactory webSqlSessionFactory(@Qualifier("webDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    @Bean(name = "webTransactionManager")
    public PlatformTransactionManager webTransactionManager(@Qualifier("webDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
