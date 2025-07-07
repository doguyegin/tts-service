package com.example.audioengine.controller;

import com.example.audioengine.service.AudioMatcherService;
import com.example.audioengine.service.AudioMergeService;
import com.example.audioengine.service.TextParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioController {
    private static final Logger logger = LoggerFactory.getLogger(AudioController.class);
    
    @Autowired private TextParserService parserService;
    @Autowired private AudioMatcherService matcherService;
    @Autowired private AudioMergeService mergeService;

    @PostMapping("/speak")
    public ResponseEntity<Resource> speak(@RequestBody Map<String, String> payload) {
        long startTime = System.currentTimeMillis();
        String requestId = java.util.UUID.randomUUID().toString();
        
        try {
            logger.info("[REQUEST-{}] Audio synthesis request started", requestId);
            
            String input = payload.get("text");
            if (input == null || input.trim().isEmpty()) {
                logger.warn("[REQUEST-{}] Empty or null text input received", requestId);
                return ResponseEntity.badRequest().build();
            }
            
            logger.info("[REQUEST-{}] Processing text: '{}'", requestId, input);
            
            // Parse text
            List<String> words = parserService.parseText(input);
            logger.info("[REQUEST-{}] Parsed {} words: {}", requestId, words.size(), words);
            
            // Match audio files
            List<File> audioFiles = matcherService.matchWordsToAudio(words);
            logger.info("[REQUEST-{}] Found {} matching audio files", requestId, audioFiles.size());
            
            if (audioFiles.isEmpty()) {
                logger.warn("[REQUEST-{}] No audio files found for input: '{}'", requestId, input);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
            }
            
            // Merge audio files
            File merged = mergeService.mergeAudioFiles(audioFiles);
            logger.info("[REQUEST-{}] Audio files merged successfully: {}", requestId, merged.getAbsolutePath());
            
            // Create response
            InputStreamResource resource = new InputStreamResource(new FileInputStream(merged));
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("[REQUEST-{}] Request completed successfully in {}ms", requestId, duration);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=output.wav")
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(resource);
                
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[REQUEST-{}] IO Error during audio processing: {} (duration: {}ms)", 
                requestId, e.getMessage(), duration, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("[REQUEST-{}] Unexpected error during audio processing: {} (duration: {}ms)", 
                requestId, e.getMessage(), duration, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 