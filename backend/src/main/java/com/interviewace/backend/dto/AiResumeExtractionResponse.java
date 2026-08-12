package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
public record AiResumeExtractionResponse(@JsonProperty("file_name") String fileName,
        @JsonProperty("character_count") Integer characterCount,
        @JsonProperty("page_count") Integer pageCount,
        @JsonProperty("extracted_text") String extractedText) {}
