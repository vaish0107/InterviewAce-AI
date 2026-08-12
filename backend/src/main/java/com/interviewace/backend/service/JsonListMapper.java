package com.interviewace.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewace.backend.exception.ResumeProcessingException;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class JsonListMapper {
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};
    private final ObjectMapper objectMapper;
    public JsonListMapper(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
    public String serialize(List<String> values) {
        try { return objectMapper.writeValueAsString(values == null ? List.of() : values); }
        catch (JsonProcessingException exception) { throw new ResumeProcessingException("Unable to store resume analysis", exception); }
    }
    public List<String> deserialize(String value) {
        if (value == null || value.isBlank()) return List.of();
        try { return objectMapper.readValue(value, STRING_LIST); }
        catch (JsonProcessingException exception) { throw new ResumeProcessingException("Stored resume analysis is invalid", exception); }
    }
}
