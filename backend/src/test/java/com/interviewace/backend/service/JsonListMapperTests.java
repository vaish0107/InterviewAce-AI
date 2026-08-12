package com.interviewace.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonListMapperTests {
    @Test void preservesValuesContainingCommas() {
        JsonListMapper mapper = new JsonListMapper(new ObjectMapper());
        List<String> values = List.of("Java, Spring", "Clear impact, with metrics");
        assertEquals(values, mapper.deserialize(mapper.serialize(values)));
    }
    @Test void mapsNullAndBlankToEmptyLists() {
        JsonListMapper mapper = new JsonListMapper(new ObjectMapper());
        assertEquals(List.of(), mapper.deserialize(null));
        assertEquals("[]", mapper.serialize(null));
    }
}
