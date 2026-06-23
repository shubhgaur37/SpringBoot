package com.shubh.module4.Prod_Ready_Features.service.impl;

import com.shubh.module4.Prod_Ready_Features.dto.PostDTO;
import com.shubh.module4.Prod_Ready_Features.entity.PostEntity;
import com.shubh.module4.Prod_Ready_Features.repository.PostRepository;
import com.shubh.module4.Prod_Ready_Features.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;


@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {
    PostRepository postRepository;
    ModelMapper modelMapper;

    @Override
    public List<PostDTO> getAllPosts() {
        return postRepository
                .findAll()
                .stream()
                .map(post -> modelMapper.map(post,PostDTO.class))
                .toList();
    }

    @Override
    public PostDTO createNewPost(PostDTO inputPost) {
        PostEntity postEntity = modelMapper.map(inputPost,PostEntity.class);
        return modelMapper.map(postRepository.save(postEntity),PostDTO.class);
    }

}
