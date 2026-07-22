package com.Shubh.Module7.M7_TestingMethodologies.service.impl;

import com.Shubh.Module7.M7_TestingMethodologies.service.DataService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


@Service
// Creates this bean only when the "PROD" profile is active. Without an active
// profile, Spring uses the default profile, so an unprofiled DataService bean
// (if present) will be injected instead.
@Profile("PROD")
public class ProdDataService implements DataService {
    @Override
    public String getData() {
        return "PROD_DATA";
    }

    @Override
    public String getEnvironment() {
        return "PROD";
    }
}
