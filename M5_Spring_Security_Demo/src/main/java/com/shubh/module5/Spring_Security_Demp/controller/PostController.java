package com.shubh.module5.Spring_Security_Demp.controller;

import com.shubh.module5.Spring_Security_Demp.dto.PostDTO;
import com.shubh.module5.Spring_Security_Demp.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/posts")
public class PostController {

    PostService postService;

    @GetMapping
    public List<PostDTO> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping(path = "/{id}")
    public PostDTO getPostById(@PathVariable(name = "id") Long id) {
        return postService.getPostById(id);
    }

    @PostMapping
    public PostDTO createPost(@RequestBody PostDTO inputPost) {
        return postService.createNewPost(inputPost);
    }
}
