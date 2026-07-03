package com.shubh.module5.Spring_Security_Demp.service;

import com.shubh.module5.Spring_Security_Demp.dto.PostDTO;

import java.util.List;


public interface PostService {

    List<PostDTO> getAllPosts();

    PostDTO createNewPost(PostDTO inputPost);

    PostDTO getPostById(Long id);
}
