package com.shubh.module5.Spring_Security_Demo.entity;

import com.shubh.module5.Spring_Security_Demo.entity.enums.Role;
import com.shubh.module5.Spring_Security_Demo.entity.enums.SubscriptionPlan;
import com.shubh.module5.Spring_Security_Demo.utils.RolePermissionMapper;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString // for logging
public class UserEntity implements UserDetails {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    Long id;

    String name;

    @Column(nullable = false, unique = true)
    String email;

    String password;

    //Long sessionLimit; // Added as a part of subsription now

    // Store the user's current subscription plan directly as an enum.
    // @ManyToOne is not appropriate because SubscriptionPlan is an enum,
    // not a JPA entity. Relationship annotations model associations
    // between entities stored in separate database tables.
    //
    // No default value is assigned here. The initial subscription plan is a
    // business rule and is therefore assigned in the service/registration
    // flow (e.g. SignUpDTO or OAuth signup), not in the entity itself.
    // Entities should represent persisted state and avoid containing
    // business-specific initialization logic.
    @Enumerated(EnumType.STRING)
    SubscriptionPlan plan;

    /**
     * Stores the user's roles as an Element Collection.
     * <p>
     * Why @ElementCollection instead of @ManyToMany?
     * ------------------------------------------------
     * Roles in this application are fixed enum values (ADMIN, USER, etc.)
     * and do not exist as independent entities. JPA therefore creates a
     * separate table (e.g. user_roles) containing:
     * <p>
     * user_id | role
     * ----------------
     * 1    | ADMIN
     * 1    | USER
     * <p>
     * Since Role is just a value and not an entity, no Role table or
     * join entity is required.
     * <p>
     * A @ManyToMany relationship would be preferred if Role itself became
     * an entity with additional information such as:
     * - description
     * - display name
     * - permissions
     * - created/updated timestamps
     * <p>
     * Typical enterprise RBAC model:
     * <p>
     * User <--ManyToMany--> Role <--ManyToMany--> Permission
     * <p>
     * where both Role and Permission are managed dynamically from the
     * database instead of being hardcoded enums.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)

    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role") // originally roles
    private Set<Role> roles;

    /**
     * Converts application roles into Spring Security GrantedAuthorities.
     * <p>
     * Spring Security distinguishes between Roles and Authorities.
     * Calling:
     * <p>
     * hasRole("ADMIN")
     * <p>
     * internally checks for the authority:
     * <p>
     * ROLE_ADMIN
     * <p>
     * Therefore each Role enum is prefixed with "ROLE_" before being
     * wrapped as a SimpleGrantedAuthority.
     * <p>
     * If the application instead used hasAuthority("ADMIN"), the prefix
     * would not be required.
     */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return roles.stream()

                // Iterate through every role assigned to the user.
                // Each role expands into multiple authorities:
                //   1. The role itself (ROLE_ADMIN, ROLE_USER, ...)
                //   2. All permissions mapped to that role (POST_CREATE, USER_VIEW, ...)
                // flatMap() combines the authorities generated for every role into
                // one continuous stream.
                .flatMap(role -> Stream.concat(

                        // Add the role as a Spring Security authority.
                        // hasRole(...) expects authorities prefixed with "ROLE_".
                        Stream.of(new SimpleGrantedAuthority("ROLE_" + role.name())),

                        // Get all permissions for the current role, convert the
                        // Set<Permission> into a stream, and map each permission
                        // to a SimpleGrantedAuthority.
                        RolePermissionMapper.getRolePermissions(role).stream()
                                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                ))

                // Collect all generated authorities into a Set to remove duplicates.
                .collect(Collectors.toSet());
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
