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
 * ServiceDB DataSource Configuration (PostgreSQL)
 * Scans service layer mappers: com.bsmartben.puml.service.*.mapper
 */
@Configuration
@MapperScan(
    basePackages = {"com.bsmartben.puml.service.*.mapper"},
    sqlSessionFactoryRef = "serviceSqlSessionFactory"
)
public class ServiceDataSourceConfig {

    @Bean(name = "serviceDataSource")
    @ConfigurationProperties(prefix = "app.datasource.service")
    public DataSource serviceDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "serviceSqlSessionFactory")
    public SqlSessionFactory serviceSqlSessionFactory(@Qualifier("serviceDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    @Bean(name = "serviceTransactionManager")
    public PlatformTransactionManager serviceTransactionManager(@Qualifier("serviceDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
