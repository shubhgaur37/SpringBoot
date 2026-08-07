package com.shubh.module9.spring_ai_demo.service;


import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
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

    @Test
    void testIngestDataAndDoSimilaritySearch(){
        aiService.ingestComplexDataToVectorStore();
        String criteria = "space";
        // get documents
        List<Document> documents = aiService.similaritySearch(criteria);
        documents.forEach(document -> System.out.println(document +"\n"));
    }

    @Test
    void testSimilaritySearchTopK(){
        String criteria = "space";
        int k = 5;
        List<Document> documents = aiService.getTopKSimilaritySearch(criteria,k);
        documents.forEach(document -> System.out.println(document +"\n"));
    }

    @Test
    void testSimilaritySearchFiltering(){
        String criteria = "space";
        String directorFilter = "Christopher";

        // no results as we do
        List<Document> documents = aiService.similaritySearchExactFiltering(criteria, directorFilter);
        documents.forEach(document -> System.out.println(document +"\n"));

    }


    @Test
    void testSimilaritySearchFilteringExpression() {

        String criteria = "space";

        // Metadata filter expression.
        //
        // Supported operators:
        //   ==  !=  >  >=  <  <=
        //   && (AND)
        //   || (OR)
        //
        // String values must be enclosed in quotes.
        // Numeric values do not require quotes.
        //
        // Returns documents where:
        //      director == "Christopher Nolan"
        //              OR
        //      year >= 2010
        //
        // This filtering is performed by the underlying VectorStore before
        // similarity search results are returned, reducing the search space.
        //
        // For more fine-grained filtering (e.g. substring matching, regular
        // expressions, custom business rules, fuzzy matching, etc.), retrieve the
        // candidate documents first and filter them in Java using the Document
        // objects and their metadata.
        String filterExpression =
                "director == 'Christopher Nolan' || year >= 2010";

        List<Document> documents =
                aiService.similaritySearchStringExpressionFilter(criteria, filterExpression);

        documents.forEach(document -> System.out.println(document + "\n"));
    }

}