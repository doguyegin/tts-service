package com.example.audioengine.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TextParserService {
    public List<String> parseText(String input) {
        return Arrays.asList(input.toLowerCase().split("\\s+"));
    }
} 