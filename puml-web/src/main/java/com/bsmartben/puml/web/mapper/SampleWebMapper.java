package com.bsmartben.puml.web.mapper;

import org.apache.ibatis.annotations.Select;

/**
 * Sample Mapper for WebDB (MySQL)
 * This mapper will be bound to webSqlSessionFactory
 */
public interface SampleWebMapper {
    
    /**
     * Test MySQL connection
     * @return 1 if connection is successful
     */
    @Select("SELECT 1")
    Integer testConnection();
}
