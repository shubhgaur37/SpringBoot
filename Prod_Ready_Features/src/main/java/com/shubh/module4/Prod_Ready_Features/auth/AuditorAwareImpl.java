package com.shubh.module4.Prod_Ready_Features.auth;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

// Using String with auditor aware because we want to return usernames[string datatype]
// can made to work with different types, Long for id's etc.
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        // complete and concise implementation with spring security
        // get security context
        // get authentication
        // get principle
        // get username

        // hardcoding to see behaviour
        return Optional.of("Shubh");
    }
}
