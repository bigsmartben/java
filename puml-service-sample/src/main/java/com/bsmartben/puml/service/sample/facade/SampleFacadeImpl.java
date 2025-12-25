package com.bsmartben.puml.service.sample.facade;

import com.bsmartben.puml.api.dto.SampleDTO;
import com.bsmartben.puml.service.sample.mapper.SampleServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of SampleFacade
 */
@Service
public class SampleFacadeImpl implements SampleFacade {

    @Autowired
    private SampleServiceMapper sampleServiceMapper;

    @Override
    public SampleDTO getSampleData() {
        SampleDTO dto = new SampleDTO();
        dto.setId(1L);
        dto.setName("Sample");
        dto.setDescription("Sample data from service layer");
        return dto;
    }

    @Override
    @Transactional("serviceTransactionManager")
    public Integer testServiceDb() {
        return sampleServiceMapper.testConnection();
    }
}
