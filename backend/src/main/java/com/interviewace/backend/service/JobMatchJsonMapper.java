package com.interviewace.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewace.backend.dto.JobMatchCategoryDto;
import com.interviewace.backend.exception.ResumeProcessingException;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class JobMatchJsonMapper {
    private static final TypeReference<Map<String, JobMatchCategoryDto>> CATEGORY_MAP = new TypeReference<>() {};
    private final ObjectMapper objectMapper;
    public JobMatchJsonMapper(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
    public String serialize(Map<String, JobMatchCategoryDto> values) {
        try { return objectMapper.writeValueAsString(values == null ? Map.of() : values); }
        catch (JsonProcessingException exception) { throw new ResumeProcessingException("Unable to store job match analysis", exception); }
    }
    public Map<String, JobMatchCategoryDto> deserialize(String value) {
        if (value == null || value.isBlank()) return Map.of();
        try { return objectMapper.readValue(value, CATEGORY_MAP); }
        catch (JsonProcessingException exception) { throw new ResumeProcessingException("Stored job match analysis is invalid", exception); }
    }
}
