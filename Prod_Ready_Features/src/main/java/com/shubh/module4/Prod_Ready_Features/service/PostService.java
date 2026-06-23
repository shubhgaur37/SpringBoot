package com.shubh.module4.Prod_Ready_Features.service;

import com.shubh.module4.Prod_Ready_Features.dto.PostDTO;

import java.util.List;


public interface PostService {

    List<PostDTO> getAllPosts();

    PostDTO createNewPost(PostDTO inputPost);

    PostDTO getPostById(Long id);
}
