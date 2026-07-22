package com.Shubh.Module7.M7_TestingMethodologies.service.impl;

import com.Shubh.Module7.M7_TestingMethodologies.service.DataService;

public class ProdDataService implements DataService {
    @Override
    public String getData() {
        return "PROD";
    }
}
