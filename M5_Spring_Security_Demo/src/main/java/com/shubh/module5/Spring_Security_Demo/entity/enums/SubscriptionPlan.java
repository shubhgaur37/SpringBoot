package com.shubh.module5.Spring_Security_Demo.entity.enums;

public enum SubscriptionPlan {
    // Free tier for new users.
    // Limited post creation and only one active session.
    FREE,

    // Paid starter tier.
    // Increased limits suitable for regular users.
    BASIC,

    // Full-featured subscription.
    // Unlimited usage with higher concurrent session limits.
    PREMIUM
}
