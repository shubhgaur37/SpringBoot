package com.ThreadPool.M_18_ThreadPooling;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailController {
    EmailService emailService;

    @PostMapping(path = "/emails/{email}")
    public ResponseEntity<Void> sendEmail(@PathVariable String email) {
        // Registers the delayed task and returns immediately without waiting ten seconds.
        emailService.sendEmail(email);
        return ResponseEntity.noContent().build();
    }
}
