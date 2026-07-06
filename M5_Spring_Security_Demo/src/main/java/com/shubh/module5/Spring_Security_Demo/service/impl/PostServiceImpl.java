package com.shubh.module5.Spring_Security_Demo.service.impl;

import com.shubh.module5.Spring_Security_Demo.dto.PostDTO;
import com.shubh.module5.Spring_Security_Demo.entity.PostEntity;
import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import com.shubh.module5.Spring_Security_Demo.exception.ResourceNotFoundException;
import com.shubh.module5.Spring_Security_Demo.repository.PostRepository;
import com.shubh.module5.Spring_Security_Demo.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j()
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
                .map(post -> modelMapper.map(post, PostDTO.class))
                .toList();
    }

    @Override
    public PostDTO createNewPost(PostDTO inputPost) {
        PostEntity postEntity = modelMapper.map(inputPost, PostEntity.class);
        return modelMapper.map(postRepository.save(postEntity), PostDTO.class);
    }

    @Override
    public PostDTO getPostById(Long id) {
        UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // getting authenticated user from security context
        log.info("User {} wants to fetch post with id: {}", user, id);
        PostEntity post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        return modelMapper.map(post, PostDTO.class);
    }

}
