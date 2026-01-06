package com.bsmartben.puml.service.sample.mapper;

import org.apache.ibatis.annotations.Select;

/**
 * Sample Mapper for ServiceDB (PostgreSQL)
 * This mapper will be bound to serviceSqlSessionFactory
 */
public interface SampleServiceMapper {
    
    /**
     * Test PostgreSQL connection
     * @return 1 if connection is successful
     */
    @Select("SELECT 1")
    Integer testConnection();
}
