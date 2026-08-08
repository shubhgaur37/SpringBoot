package com.shubh.module9.spring_ai_demo.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RAGService {
    VectorStore vectorStore;
    ChatClient chatClient;


    public void answerSpringAIQuestionIntelligently(String userPrompt){
        ingestSpringAIDataToVectorStore();

        // Relevant context is retrieved from the VectorStore based on the user's
        // query instead of sending the entire knowledge base to the LLM.
        // This helps reduce the amount of context sent to the model and therefore
        // helps manage the model's context window.
        String systemPrompt = """
    You are a helpful and knowledgeable software developer.

    Answer the user's question using ONLY the information provided in the
    Context. Do not use your own knowledge or any information outside the
    Context.

    If the Context does not contain enough information to answer the
    question, state : I don't know

    Combine information from multiple Context sections when relevant.

    Context:
    {context}
    """;

        // Find the most relevant documents from the VectorStore using semantic
        // similarity between the user's query and the stored document embeddings.
        //
        // In a real-world VectorStore, documents can belong to different domains,
        // so metadata filtering can be used to restrict the search to a particular
        // knowledge base.
        //
        // The similarity threshold specifies the minimum similarity score required
        // for a document to be included in the results.
        SearchRequest searchRequest = SearchRequest.builder().query(userPrompt)
                .similarityThreshold(0.5).topK(2).build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        // Extract the text from all retrieved documents and combine it into a
        // single context string that can be provided to the LLM.
        //
        // Multiple documents are separated by a blank line so that the individual
        // context sections remain distinguishable to the model.
        String context = documents.stream().map(document -> document.getText()).collect(Collectors.joining("\n\n"));

        // Create a template so that the dynamically retrieved context can be
        // inserted into the {context} placeholder of the system prompt.
        PromptTemplate systemPromptTemplate = new PromptTemplate(systemPrompt);

        // Add the relevant document knowledge to the system prompt before sending
        // the request to the LLM.
        systemPrompt = systemPromptTemplate.render(Map.of("context",context));


        // Send both the system instructions and the original user question to the
        // LLM. The model uses the retrieved context provided in the system prompt
        // to formulate the answer.
        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .advisors(new SimpleLoggerAdvisor())
                .call()
                .content();

        System.out.println(answer);
    }
    private void ingestSpringAIDataToVectorStore(){

        // In a real-world RAG application, ingestion is typically handled
        // independently from the service responsible for answering user queries.
        // A separate ingestion pipeline/service can continuously build and update
        // the knowledge base using feedback loops, scheduled ingestion, document
        // updates, or other mechanisms.
        //
        // The ingestion process is responsible for preparing and storing knowledge
        // in the VectorStore, while the RAG/search service is responsible for
        // retrieving relevant context and generating answers.
        //
        // For this demo, ingestion is performed here only for convenience. The
        // delete operation ensures that the same demo documents are not inserted
        // repeatedly into the VectorStore every time this method is executed.
        vectorStore.delete("technology == 'Spring AI'");

        // Create the documents that form the Spring AI knowledge base.
        //
        // In a real-world application, these documents would typically come from
        // the independent ingestion pipeline rather than being created directly
        // inside the query/answering service.
        //
        // Each document contains:
        // 1. The actual knowledge/content used for semantic search.
        // 2. Metadata that can later be used for filtering.
        List<Document> springAIDocuments = List.of(

                new Document(
                        """
                        Spring AI provides abstractions for interacting with Large
                        Language Models. The ChatModel interface represents the
                        underlying AI model and provides a common abstraction across
                        different model providers such as Ollama and OpenAI.
    
                        Different providers can implement the ChatModel interface,
                        allowing application code to remain independent of the
                        specific model provider.
                        """,
                        Map.of(
                                "topic", "ChatModel",
                                "technology", "Spring AI"
                        )
                ),

                new Document(
                        """
                        ChatClient is a high-level API in Spring AI used by
                        applications to interact with ChatModels.
    
                        It provides a fluent API for constructing prompts and
                        sending requests to an AI model. A ChatClient can be
                        configured with a ChatModel and can be used to provide
                        user prompts, system prompts, advisors and other options.
                        """,
                        Map.of(
                                "topic", "ChatClient",
                                "technology", "Spring AI"
                        )
                ),

                new Document(
                        """
                        VectorStore is an abstraction in Spring AI for storing and
                        searching documents using vector embeddings.
    
                        Documents are converted into embeddings and stored in a
                        vector database. A similarity search can then retrieve
                        documents that are semantically related to a query.
    
                        Spring AI provides implementations of VectorStore for
                        different vector databases, including PGVector.
                        """,
                        Map.of(
                                "topic", "VectorStore",
                                "technology", "Spring AI"
                        )
                ),

                new Document(
                        """
                        EmbeddingModel is the Spring AI abstraction responsible for
                        converting text into numerical vector representations called
                        embeddings.
    
                        These embeddings capture the semantic meaning of the text
                        and allow a VectorStore to perform similarity searches.
                        Different AI providers can provide implementations of the
                        EmbeddingModel interface.
                        """,
                        Map.of(
                                "topic", "EmbeddingModel",
                                "technology", "Spring AI"
                        )
                ),

                new Document(
                        """
                        A typical Spring AI flow is:
    
                        Application
                            ->
                        ChatClient
                            ->
                        ChatModel
                            ->
                        AI Model Provider
    
                        For RAG and semantic search, documents follow a different
                        path:
    
                        Document
                            ->
                        EmbeddingModel
                            ->
                        VectorStore
                            ->
                        Similarity Search
                        """,
                        Map.of(
                                "topic", "Architecture",
                                "technology", "Spring AI"
                        )
                )
        );

        // Convert the documents into embeddings and store them in the configured
        // VectorStore so that they can be retrieved later using semantic search.
        vectorStore.add(springAIDocuments);
    }


}
