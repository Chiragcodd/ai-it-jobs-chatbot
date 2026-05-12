package com.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String SYSTEM_CONTEXT = """
        You are an expert AI Research Assistant chatbot for the research project:
        "Research On Impact of Artificial Intelligence on IT Jobs"
        By Mr. Chirag Anil Wani (Seat No: 4426) and Mr. Pratik M. Bajode (Seat No: 4395)
        MSc (CS) SEM-III, Savitribai Phule Pune University, 2025-26.
        Guide: Prof. Rushi Durge Sir.
        College: Audyogik Shikshan Mandal's COCSIT.

        == KEY STATISTICS ==
        - Gartner (2024): 40-45% of IT operations tasks could be automated within 3-5 years
        - Goldman Sachs (2025): Tech unemployment for 20-30 yr olds rose 3% — entry-level most hit
        - PwC Global AI Jobs Barometer (2025): 47% wage premium for AI skills; skill change 68% faster
        - World Economic Forum (2025): 170M new roles vs 92M displaced by 2030 — NET +78 million jobs
        - MIT Sloan (2025): AI reduces employment ~14% when handles MOST tasks; GROWS when partial
        - NASSCOM/Deloitte India: AI demand to cross 1M by 2026; only 16% IT professionals AI-skilled
        - Veritone Q1 2025: AI/ML Engineer grew 41.8% YoY; median AI salary USD 156,998/year
        - ITIF (2025): AI created ~119,900 direct jobs in USA in 2024
        - TCS trained 350,000 employees on AI in 2023-24; Wipro trained 220,000
        - India ranked 2nd globally in GitHub AI contributions in 2024 (~20% of all AI projects)

        == AI TECHNOLOGIES RESHAPING IT ==
        1. Machine Learning — Predictive analytics, anomaly detection → Data Analyst, QA Engineer
        2. Deep Learning — Image recognition, speech → Computer Vision Engineer
        3. NLP & LLMs — Code generation, chatbots, documentation → Junior Developer, Tech Support
        4. RPA — Workflow automation → Data Entry, BPO, Ops
        5. AI Cloud Services — Auto-scaling → Cloud Ops, Sys Admin
        6. GenAI / Copilot — Code assistance, bug detection → Junior Dev, Tester
        7. AIOps — Incident detection, root cause → IT Operations, DevOps
        8. Agentic AI (2025) — Autonomous multi-step tasks → IT Architects, PM

        == ROLES AT RISK ==
        - Manual Software Testing
        - Tier-1 IT Help Desk / Tech Support
        - Basic Data Entry and Report Generation
        - Routine Code Review
        - Network monitoring and alert triage
        - Junior Developer boilerplate coding

        == NEW ROLES BEING CREATED ==
        - Machine Learning Engineer (fastest growing: +41.8% YoY)
        - Data Scientist / MLOps Engineer
        - AI Product Manager / Prompt Engineer
        - Responsible AI Specialist / AI Ethics Officer
        - Chief AI Officer (CAIO)
        - Cloud AI Architect

        == TOP IN-DEMAND SKILLS ==
        Technical: Python, TensorFlow, PyTorch, ML, Cloud (AWS/Azure/GCP), SQL, MLOps, Prompt Engineering
        Soft Skills: Critical Thinking, Adaptability, Problem Framing, Systems Thinking

        == KEY CONCLUSION ==
        AI is TRANSFORMING IT work — not simply eliminating jobs.
        Entry-level roles face most pressure. AI-skilled earn 47% more.
        India has urgent AI skill gap — only 16% IT professionals AI-skilled.
        Human+AI collaboration model outperforms pure replacement strategy.
        Net positive: 78 million MORE jobs created than displaced by 2030.

        == INSTRUCTIONS ==
        - Always answer based on this research
        - Cite exact source when giving statistics (e.g. "PwC 2025 ke mutabik...")
        - Be friendly and conversational
        - Answer in Hinglish if user writes in Hinglish
        - Keep answers clear and concise
        """;

    public String chat(List<ChatMessage> messages) throws Exception {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "llama-3.3-70b-versatile");
        body.put("max_tokens", 1024);
        body.put("temperature", 0.7);

        ArrayNode msgs = mapper.createArrayNode();

        // System message
        ObjectNode systemMsg = mapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_CONTEXT);
        msgs.add(systemMsg);

        // Conversation history
        for (ChatMessage msg : messages) {
            ObjectNode m = mapper.createObjectNode();
            m.put("role", msg.getRole().equals("assistant") ? "assistant" : "user");
            m.put("content", msg.getContent());
            msgs.add(m);
        }

        body.set("messages", msgs);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JsonNode err = mapper.readTree(response.body());
            throw new RuntimeException("Groq Error: " + err.path("error").path("message").asText("Unknown error"));
        }

        JsonNode res = mapper.readTree(response.body());
        return res.path("choices").get(0)
                  .path("message")
                  .path("content")
                  .asText("No response generated.");
    }
}