package com.bsmartben.puml.web;

import com.bsmartben.puml.service.sample.mapper.SampleServiceMapper;
import com.bsmartben.puml.web.mapper.SampleWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test to verify mappers are bound to correct SqlSessionFactory
 */
@SpringBootTest
class MapperBindingTest {

    @Autowired
    private SampleWebMapper sampleWebMapper;

    @Autowired
    private SampleServiceMapper sampleServiceMapper;

    /**
     * Test that web mapper is bound to webSqlSessionFactory
     */
    @Test
    void testWebMapperConnection() {
        assertNotNull(sampleWebMapper, "SampleWebMapper should be autowired");
        Integer result = sampleWebMapper.testConnection();
        assertEquals(1, result, "Web mapper should return 1");
    }

    /**
     * Test that service mapper is bound to serviceSqlSessionFactory
     */
    @Test
    void testServiceMapperConnection() {
        assertNotNull(sampleServiceMapper, "SampleServiceMapper should be autowired");
        Integer result = sampleServiceMapper.testConnection();
        assertEquals(1, result, "Service mapper should return 1");
    }
}
