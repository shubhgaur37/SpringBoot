package com.shubh.module4.Prod_Ready_Features.service.impl;

import com.shubh.module4.Prod_Ready_Features.dto.PostDTO;
import com.shubh.module4.Prod_Ready_Features.repository.PostRepository;
import com.shubh.module4.Prod_Ready_Features.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    PostRepository postRepository;

    @Override
    public List<PostDTO> getAllPosts() {
        return List.of();
    }

    @Override
    public PostDTO createNewPost(PostDTO inputPost) {
        return null;
    }
}
