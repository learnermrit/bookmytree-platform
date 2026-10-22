package com.bookmytree.platform.ai.service;

import com.bookmytree.platform.ai.dto.AiChatResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiConciergeService {

    private final ChatClient chatClient;
    private final RagKnowledgeService ragKnowledgeService;

    public AiConciergeService(ChatClient.Builder chatClientBuilder, RagKnowledgeService ragKnowledgeService) {
        this.ragKnowledgeService = ragKnowledgeService;
        this.chatClient = chatClientBuilder
                .defaultSystem("You are 'Arbor', an expert agricultural AI concierge for the 'Book My Tree' Agri-Metaverse platform. " +
                        "Use the provided context retrieved from our agricultural knowledge base to answer user questions accurately. " +
                        "If the answer is not in the context, use your general agricultural knowledge.")
                .build();
    }

    public AiChatResponseDto chatWithConcierge(String userMessage) {
        log.info("Processing RAG query for user: {}", userMessage);

        // 1. Retrieve relevant documents from PGVector store using free local embeddings
        List<Document> relevantDocs = ragKnowledgeService.searchRelevantKnowledge(userMessage);
        String context = relevantDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n---\n"));

        // 2. Prompt Gemini with augmented context
        String prompt = String.format("Context information:\n%s\n\nUser Question: %s", context, userMessage);

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return AiChatResponseDto.builder()
                .response(aiResponse)
                .modelUsed("gemini-2.5-flash + pgvector + local-transformers")
                .build();
    }
}