package com.shubh.module4.Prod_Ready_Features.controller;

import com.shubh.module4.Prod_Ready_Features.dto.PostDTO;
import com.shubh.module4.Prod_Ready_Features.service.PostService;
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
    public List<PostDTO> getAllPosts(){
        return postService.getAllPosts();
    }

    @PostMapping
    public PostDTO createPost(@RequestBody PostDTO inputPost){
        return postService.createNewPost(inputPost);
    }
}
