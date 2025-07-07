package com.example.audioengine.service;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextParserServiceTest {
    private final TextParserService parserService = new TextParserService();

    @Test
    void parseText_shouldSplitWords() {
        String input = "Merhaba dünya";
        List<String> result = parserService.parseText(input);
        assertEquals(List.of("merhaba", "dünya"), result);
    }

    @Test
    void parseText_shouldHandleEmptyInput() {
        String input = "";
        List<String> result = parserService.parseText(input);
        assertEquals(List.of(""), result);
    }
} 