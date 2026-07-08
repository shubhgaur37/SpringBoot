package com.shubh.module5.Spring_Security_Demo.controller;

import com.shubh.module5.Spring_Security_Demo.dto.PostDTO;
import com.shubh.module5.Spring_Security_Demo.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class PostController {

    PostService postService;

    @GetMapping
    // @PreAuthorize(...) evaluates a Spring Expression Language (SpEL)
    // expression, allowing built-in security methods such as hasRole(),
    // hasAnyRole(), hasAuthority(), hasAnyAuthority(), isAuthenticated(), etc.
    //
    // Logical operators such as 'and' (&&), 'or' (||) and 'not' (!)
    // can be used to compose complex authorization rules. The keyword
    // operators are case-insensitive (e.g. 'or', 'OR', 'Or' are all valid).
    @PreAuthorize("hasRole('ADMIN') OR hasAuthority('POST_VIEW')")
    public List<PostDTO> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    // Custom Spring beans can be invoked using '@beanName', where the bean
    // name defaults to the class name in camelCase
    // (PostSecurityService -> postSecurityService).
    //
    // Method arguments can be referenced by their parameter names using
    // '#parameterName'. Multiple arguments can also be passed to the bean
    // method (e.g. @postSecurityService.canEdit(#postId, #userId)).
    @PreAuthorize("@postSecurityService.isOwnerOfPost(#id)")
    public PostDTO getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    /*
     * Authorization Flow:
     *
     * HTTP Request
     *      │
     *      ▼
     * JWT Filter (validates JWT and populates SecurityContextHolder)
     *      │
     *      ▼
     * AuthorizationFilter (ensures the request is authenticated)
     *      │
     *      ▼
     * Controller
     *      │
     *      ▼
     * @Secured / @PreAuthorize (business-specific authorization)
     *      │
     *      ├── Authorized ─────────► Service
     *      │
     *      └── Access Denied (403 Forbidden)
     *
     * Authentication is enforced before reaching the controller, while
     * fine-grained authorization is delegated to method security.
     */
    @PostMapping
    // @Secured(...) performs only role-based authorization. Unlike hasRole(...),
    // it does not add the "ROLE_" prefix automatically, so the complete role
    // authority must be specified explicitly.
    @Secured({"ROLE_ADMIN", "ROLE_CREATOR"})
    public PostDTO createPost(@RequestBody PostDTO inputPost) {
        return postService.createNewPost(inputPost);
    }
}