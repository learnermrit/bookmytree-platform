package com.bookmytree.platform.ai.controller;

import com.bookmytree.platform.ai.dto.AiChatRequestDto;
import com.bookmytree.platform.ai.dto.AiChatResponseDto;
import com.bookmytree.platform.ai.dto.KnowledgeIngestRequestDto;
import com.bookmytree.platform.ai.service.AiConciergeService;
import com.bookmytree.platform.ai.service.RagKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiConciergeController {

    private final AiConciergeService aiConciergeService;
    private final RagKnowledgeService ragKnowledgeService;

    @PostMapping("/concierge/chat")
    public ResponseEntity<AiChatResponseDto> chatWithConcierge(@Valid @RequestBody AiChatRequestDto request) {
        AiChatResponseDto response = aiConciergeService.chatWithConcierge(request.getMessage());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/knowledge/ingest")
    public ResponseEntity<String> ingestKnowledge(@Valid @RequestBody KnowledgeIngestRequestDto request) {
        ragKnowledgeService.addDocument(request.getContent(), request.getTitle());
        return ResponseEntity.ok("Knowledge document successfully embedded and stored in PGVector!");
    }
}