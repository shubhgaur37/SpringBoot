package com.shubh.module9.spring_ai_demo.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    /**
     * =========================================================================
     * APPROACH 1 (Current)
     * =========================================================================
     *
     * Used when the application interacts with only ONE AI model provider.
     *
     * Example:
     *
     *     spring-ai-starter-model-ollama
     *
     * Spring Boot's auto-configuration detects the dependency on the classpath
     * and creates:
     *
     *      OllamaChatModel
     *              │
     *              ▼
     *      ChatClient.Builder
     *
     * The ChatClient.Builder bean is automatically configured with the
     * appropriate ChatModel. Calling build() simply creates a ChatClient backed
     * by that model.
     *
     * This approach works only when exactly ONE ChatModel bean exists in the
     * Spring context.
     *
     * If multiple model providers are added (e.g. Ollama + OpenAI), Spring
     * creates multiple ChatModel beans and ChatClient.Builder can no longer
     * determine which one to use, resulting in a
     * NoUniqueBeanDefinitionException.
     *
     * In that scenario, use Approach 2 shown below.
     */
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }


    /**
     * =========================================================================
     * APPROACH 2 (Future)
     * =========================================================================
     *
     * Uncomment this section when the application needs to work with multiple
     * AI providers simultaneously.
     *
     * Example dependencies:
     *
     *      spring-ai-starter-model-ollama
     *      spring-ai-starter-model-openai
     *
     * Spring Boot now creates two ChatModel beans:
     *
     *      OllamaChatModel
     *      OpenAiChatModel
     *
     * Since multiple ChatModel beans exist, Spring cannot auto-configure a
     * single ChatClient.Builder without ambiguity.
     *
     * Instead, create one ChatClient for each model:
     *
     *      OllamaChatModel
     *              │
     *              ▼
     *      ChatClient ("ollama")
     *
     *
     *      OpenAiChatModel
     *              │
     *              ▼
     *      ChatClient ("openAI")
     *
     * Inject the required client using:
     *
     *      public AIService(@Qualifier("ollama") ChatClient chatClient)
     *
     * or
     *
     *      public AIService(@Qualifier("openAI") ChatClient chatClient)
     *
     * This allows different requests to be routed to different AI providers
     * while keeping the rest of the application independent of the underlying
     * model implementation.
     */

    /*
    @Bean(name = "ollama")
    ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }

    @Bean(name = "openAI")
    ChatClient openAIChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
    */
}