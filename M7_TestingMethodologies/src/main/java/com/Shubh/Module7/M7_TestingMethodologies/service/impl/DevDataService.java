package com.Shubh.Module7.M7_TestingMethodologies.service.impl;

import com.Shubh.Module7.M7_TestingMethodologies.service.DataService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


@Service
@Profile("DEV")
public class DevDataService implements DataService {

    @Override
    public String getData() {
        return "DEV_STAGING_DATA";
    }

    @Override
    public String getEnvironment() {
        return "DEV_STAGING";
    }
}
