package com.bsmartben.puml.web.controller;

import com.bsmartben.puml.api.dto.SampleDTO;
import com.bsmartben.puml.service.sample.facade.SampleFacade;
import com.bsmartben.puml.web.mapper.SampleWebMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample REST Controller demonstrating dual datasource usage
 */
@RestController
@RequestMapping("/api/sample")
public class SampleController {

    @Autowired
    private SampleFacade sampleFacade;

    @Autowired
    private SampleWebMapper sampleWebMapper;

    /**
     * Get sample data from service layer
     */
    @GetMapping("/data")
    public SampleDTO getSampleData() {
        return sampleFacade.getSampleData();
    }

    /**
     * Test web database (MySQL) connection
     */
    @GetMapping("/test-web-db")
    @Transactional("webTransactionManager")
    public Map<String, Object> testWebDb() {
        Map<String, Object> result = new HashMap<>();
        result.put("database", "WebDB (MySQL)");
        result.put("connection", sampleWebMapper.testConnection());
        result.put("status", "OK");
        return result;
    }

    /**
     * Test service database (PostgreSQL) connection via facade
     */
    @GetMapping("/test-service-db")
    public Map<String, Object> testServiceDb() {
        Map<String, Object> result = new HashMap<>();
        result.put("database", "ServiceDB (PostgreSQL)");
        result.put("connection", sampleFacade.testServiceDb());
        result.put("status", "OK");
        return result;
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        result.put("message", "Dual datasource application is running");
        return result;
    }
}
