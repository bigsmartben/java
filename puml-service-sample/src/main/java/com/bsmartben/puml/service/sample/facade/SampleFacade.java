package com.bsmartben.puml.service.sample.facade;

import com.bsmartben.puml.api.dto.SampleDTO;

/**
 * Sample Facade interface for service layer
 * Used by web layer to access business logic
 */
public interface SampleFacade {
    
    /**
     * Get sample data
     * @return sample DTO
     */
    SampleDTO getSampleData();
    
    /**
     * Test service database connection
     * @return test result
     */
    Integer testServiceDb();
}
