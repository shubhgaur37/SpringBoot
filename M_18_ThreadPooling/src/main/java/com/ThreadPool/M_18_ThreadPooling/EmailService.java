package com.ThreadPool.M_18_ThreadPooling;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EmailService {
    // Uses the dedicated scheduler bean instead of the @Async task executor.
    TaskScheduler scheduler;

    public void sendEmail(String email) {
        // The requested email task should run once, ten seconds from now.
        Instant scheduleTime = Instant.now().plus(10, ChronoUnit.SECONDS);
        log.info("Scheduling sending email to : {} after 10 seconds at time: {}", email, scheduleTime);
        // Scheduling returns immediately; a Task_Scheduler_ worker runs this action later.
        scheduler.schedule(() -> {
            log.info("Email sent to: {} using thread: {} at time: {}",
                    email, Thread.currentThread().getName(), Instant.now());
        }, scheduleTime);
    }
}
