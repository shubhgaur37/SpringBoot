package com.shubh.module5.Spring_Security_Demo.dto;

import com.shubh.module5.Spring_Security_Demo.entity.enums.Permission;
import com.shubh.module5.Spring_Security_Demo.entity.enums.Role;
import com.shubh.module5.Spring_Security_Demo.entity.enums.SubscriptionPlan;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignUpDTO {

    String name;
    String email;
    String password;

    /**
     * Included here for demonstration purposes.
     * <p>
     * In production applications, clients should not be allowed to specify
     * their own roles during registration (e.g. ADMIN). New users are
     * typically assigned a default role such as USER, while elevated roles
     * are granted later through secured administrative workflows.
     */
    Set<Role> roles;

    /**
     * Included here for demonstration purposes.
     * <p>
     * In production applications, permissions are generally derived from
     * the user's assigned roles or granted through administrative workflows,
     * rather than being supplied directly by the client.
     */
    Set<Permission> permissions;

    /**
     * Included here for demonstration purposes.
     * <p>
     * Defaults to the FREE subscription when omitted from the request.
     * In production, subscription plans are typically assigned by the
     * server after registration or updated only after a successful
     * subscription or payment workflow.
     */
    SubscriptionPlan plan = SubscriptionPlan.FREE;
}