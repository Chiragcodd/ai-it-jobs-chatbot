package com.chatbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private GroqService groqService;   // ← sirf yahan Gemini → Groq kiya

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request) {
        Map<String, String> response = new HashMap<>();
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            response.put("error", "Messages list is empty");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            String reply = groqService.chat(request.getMessages());
            response.put("reply", reply);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> res = new HashMap<>();
        res.put("status", "running");
        res.put("project", "AI & IT Jobs Research Chatbot - Chirag & Pratik MSc SPPU");
        return ResponseEntity.ok(res);
    }
}
