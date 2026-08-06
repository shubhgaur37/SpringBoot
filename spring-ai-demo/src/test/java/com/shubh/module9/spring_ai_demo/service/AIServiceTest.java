package com.shubh.module9.spring_ai_demo.service;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

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

    @Test
    void testGetEmbeddingForText(){
        String text = "This is my text";
        float [] embeddings = aiService.getEmbeddingsForText(text);
        System.out.println("EMBEDDINGS FOR THE TEXT");
        System.out.println(Arrays.toString(embeddings));
    }

    @Test
    void testGetEmbeddingsForTextList(){
        List<float[]> embeddingList = aiService.getEmbeddingsForListOfText(List.of("Hello","Boy"));
        System.out.println("EMBEDDING LIST FOR THE TEXT");
        embeddingList.forEach(embedding -> System.out.println(Arrays.toString(embedding)));
    }

    @Test
    void testStoreSimpleDataInVectorStore(){
        String text = "Twinkle Twinkle Little Star, How I wonder what you are";
        aiService.ingestSimpleTextDataToVectorStore(text);
    }

}