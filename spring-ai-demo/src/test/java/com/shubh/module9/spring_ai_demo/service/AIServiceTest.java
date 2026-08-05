package com.shubh.module9.spring_ai_demo.service;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AIServiceTest {
    @Autowired
    AIService aiService;

    @Test
    void testGetJoke(){
        aiService.getJoke();
    }

    @Test
    void testGetJokeByTopic(){
        String topic = "Potty";
        System.out.println(aiService.getJokeByTopic(topic));
    }

    @Test
    void testGetJokeEntityByTopic(){
        String topic = "Politics";
        System.out.println(aiService.getJokeByTopicDeserialized(topic));
    }

}