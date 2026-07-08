package com.shubh.module5.Spring_Security_Demo.utils;

import com.shubh.module5.Spring_Security_Demo.entity.enums.Permission;
import com.shubh.module5.Spring_Security_Demo.entity.enums.Role;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.shubh.module5.Spring_Security_Demo.entity.enums.Permission.*;
import static com.shubh.module5.Spring_Security_Demo.entity.enums.Role.*;

public final class RolePermissionMapper {

    private RolePermissionMapper() {
        // Prevent instantiation of utility class.
    }

    // Default permissions granted to every USER.
    private static final Set<Permission> USER_PERMISSIONS = Set.of(
            POST_VIEW,
            USER_VIEW
    );

    // Inherit all USER permissions and add creator-specific permissions.
    // Stream.concat() combines the USER permission stream with the additional
    // permissions before collecting everything into a Set.
    private static final Set<Permission> CREATOR_PERMISSIONS = Stream.concat(
            USER_PERMISSIONS.stream(),
            Stream.of(
                    POST_CREATE,
                    POST_UPDATE
            )
    ).collect(Collectors.toSet());

    // Inherit all CREATOR permissions (which already includes USER permissions)
    // and add administrator-specific permissions.
    private static final Set<Permission> ADMIN_PERMISSIONS = Stream.concat(
            CREATOR_PERMISSIONS.stream(),
            Stream.of(
                    POST_DELETE,
                    USER_CREATE,
                    USER_UPDATE,
                    USER_DELETE
            )
    ).collect(Collectors.toSet());

    // Maps every role to the complete set of permissions it possesses.
    private static final Map<Role, Set<Permission>> ROLE_PERMISSIONS = Map.of(
            USER, USER_PERMISSIONS,
            CREATOR, CREATOR_PERMISSIONS,
            ADMIN, ADMIN_PERMISSIONS
    );

    /**
     * Returns all permissions associated with the supplied role.
     * If no mapping exists, an empty permission set is returned.
     */
    public static Set<Permission> getRolePermissions(Role role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Set.of());
    }
}