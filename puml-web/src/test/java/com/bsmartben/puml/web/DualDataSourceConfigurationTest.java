package com.bsmartben.puml.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test to verify dual datasource configuration
 */
@SpringBootTest
class DualDataSourceConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Verify that both datasources are configured
     */
    @Test
    void testDualDataSourcesExist() {
        // Verify webDataSource exists
        DataSource webDataSource = applicationContext.getBean("webDataSource", DataSource.class);
        assertNotNull(webDataSource, "webDataSource should be configured");

        // Verify serviceDataSource exists
        DataSource serviceDataSource = applicationContext.getBean("serviceDataSource", DataSource.class);
        assertNotNull(serviceDataSource, "serviceDataSource should be configured");
    }

    /**
     * Verify that both SqlSessionFactory beans are configured
     */
    @Test
    void testDualSqlSessionFactoriesExist() {
        // Verify webSqlSessionFactory exists
        Object webSqlSessionFactory = applicationContext.getBean("webSqlSessionFactory");
        assertNotNull(webSqlSessionFactory, "webSqlSessionFactory should be configured");

        // Verify serviceSqlSessionFactory exists
        Object serviceSqlSessionFactory = applicationContext.getBean("serviceSqlSessionFactory");
        assertNotNull(serviceSqlSessionFactory, "serviceSqlSessionFactory should be configured");
    }

    /**
     * Verify that both transaction managers are configured
     */
    @Test
    void testDualTransactionManagersExist() {
        // Verify webTransactionManager exists
        Object webTransactionManager = applicationContext.getBean("webTransactionManager");
        assertNotNull(webTransactionManager, "webTransactionManager should be configured");

        // Verify serviceTransactionManager exists
        Object serviceTransactionManager = applicationContext.getBean("serviceTransactionManager");
        assertNotNull(serviceTransactionManager, "serviceTransactionManager should be configured");
    }

    /**
     * Verify that mappers are properly configured
     */
    @Test
    void testMappersExist() {
        // Verify web mapper exists
        Object webMapper = applicationContext.getBean("sampleWebMapper");
        assertNotNull(webMapper, "SampleWebMapper should be configured");

        // Verify service mapper exists
        Object serviceMapper = applicationContext.getBean("sampleServiceMapper");
        assertNotNull(serviceMapper, "SampleServiceMapper should be configured");
    }
}
