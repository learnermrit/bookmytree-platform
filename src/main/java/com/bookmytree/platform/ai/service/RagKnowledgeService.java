package com.bookmytree.platform.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RagKnowledgeService {

    private final VectorStore vectorStore;

    public RagKnowledgeService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void addDocument(String content, String title) {
        log.info("Embedding and saving document to PGVector store: '{}'", title);
        Document doc = new Document(content, Map.of("title", title));
        vectorStore.add(List.of(doc));
        log.info("Successfully ingested document into vector store.");
    }

    public List<Document> searchRelevantKnowledge(String query) {
        log.info("Searching PGVector store for semantic query: '{}'", query);
        return vectorStore.similaritySearch(query);
    }
}