package com.interviewace.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewace.backend.dto.JobMatchCategoryDto;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JobMatchJsonMapperTests {
    @Test void serializesAndDeserializesTypedCategoryMatches() {
        JobMatchJsonMapper mapper = new JobMatchJsonMapper(new ObjectMapper());
        Map<String, JobMatchCategoryDto> value = Map.of("backend",
                new JobMatchCategoryDto(List.of("Java", "REST API"), List.of("Java"), List.of("REST API"), 50));
        assertEquals(value, mapper.deserialize(mapper.serialize(value)));
    }
    @Test void mapsNullAndBlankToEmptyMaps() {
        JobMatchJsonMapper mapper = new JobMatchJsonMapper(new ObjectMapper());
        assertEquals("{}", mapper.serialize(null)); assertEquals(Map.of(), mapper.deserialize(""));
    }
}
