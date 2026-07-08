package com.shubh.module5.Spring_Security_Demo.entity;

import com.shubh.module5.Spring_Security_Demo.entity.enums.Permission;
import com.shubh.module5.Spring_Security_Demo.entity.enums.Role;
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

    private Set<Permission> permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // In production, a more common approach is to derive permissions from roles
        // (Role -> Permissions mapping) instead of assigning permissions directly
        // to each user.

        // The current approach allows a user to have additional custom permissions
        // alongside the permissions inherited from their roles.

        // Roles are prefixed with "ROLE_" because we explicitly create the
        // GrantedAuthority in that format. Spring Security's hasRole(...)
        // internally looks for authorities prefixed with "ROLE_".
        // Permissions are typically checked directly using hasAuthority(...).
        Set<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());

        permissions.forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.name())));

        return authorities;
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
