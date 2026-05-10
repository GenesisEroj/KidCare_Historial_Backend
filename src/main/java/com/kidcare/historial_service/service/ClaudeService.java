package com.kidcare.historial_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ClaudeService {

    @Value("${claude.api.key:}")
    private String apiKey;

    @Value("${claude.model:claude-sonnet-4-6}")
    private String model;

    private static final String CLAUDE_URL = "https://api.anthropic.com/v1/messages";

    public String generarResumenClinico(List<String> observaciones, String contextoMenor) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Claude API no configurada");
        }

        StringBuilder obsText = new StringBuilder();
        for (int i = 0; i < observaciones.size(); i++) {
            obsText.append((i + 1)).append(". ").append(observaciones.get(i)).append("\n");
        }

        String prompt = "Genera un resumen clínico estructurado en español para un médico pediatra " +
            "basado en las siguientes observaciones de un cuidador. " +
            "Incluye únicamente comportamientos y síntomas observables reportados. " +
            "NO incluyas diagnósticos, recomendaciones de tratamiento ni información personal identificable. " +
            "Usa estas secciones: Síntomas Principales, Evolución Temporal, Observaciones Adicionales.\n\n" +
            "Contexto: " + contextoMenor + "\n\n" +
            "Observaciones registradas:\n" + obsText;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 2048);
        body.put("messages", List.of(message));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(CLAUDE_URL, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<?> content = (List<?>) response.getBody().get("content");
            if (content != null && !content.isEmpty()) {
                Map<?, ?> firstBlock = (Map<?, ?>) content.get(0);
                Object textValue = firstBlock.get("text");
                if (textValue != null) {
                    return textValue.toString();
                }
            }
        }
        throw new RuntimeException("Claude no retornó respuesta válida");
    }
}
