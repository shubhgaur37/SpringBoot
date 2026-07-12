package com.shubh.module5.Spring_Security_Demo.utils;

import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import com.shubh.module5.Spring_Security_Demo.entity.enums.SubscriptionPlan;
import com.shubh.module5.Spring_Security_Demo.repository.PostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SubscriptionService {

    PostRepository postRepository;

    // Returns the maximum number of concurrent sessions allowed for a
    // subscription plan.
    //
    // This uses Java's modern switch expression (Java 14+), which:
    // - does not require break statements.
    // - does not allow fall-through between cases.
    // - returns a value directly.
    // Since all SubscriptionPlan enum constants are handled, no default
    // case is required.
    public int getSessionLimit(SubscriptionPlan plan) {

        return switch (plan) {
            case FREE -> 1;
            case BASIC -> 2;
            case PREMIUM -> 5;
        };
    }

    // Determines whether the currently authenticated user can create
    // another post based on their subscription plan.
    //
    // This method is intended to be used inside @PreAuthorize:
    //
    // @PreAuthorize("@subscriptionService.canPost()")
    //
    // Spring Security invokes this bean method before the controller
    // method executes.
    public boolean canPost() {

        // Retrieve the currently authenticated user from the
        // thread-local SecurityContext associated with this request.
        UserEntity user = (UserEntity) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Count the number of posts already created by this user.
        int totalPosts = postRepository.getPostsCount(user);

        // Compare the user's current post count against the maximum
        // allowed by their subscription plan.
        return totalPosts < switch (user.getPlan()) {
            case FREE -> 2;
            case BASIC -> 4;
            case PREMIUM -> 6;
        };
    }
}