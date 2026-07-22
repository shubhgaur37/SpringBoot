package com.Shubh.Module7.M7_TestingMethodologies.service.impl;

import com.Shubh.Module7.M7_TestingMethodologies.service.DataService;

public class DevDataService implements DataService {

    @Override
    public String getData() {
        return "DEV_STAGING";
    }
}
