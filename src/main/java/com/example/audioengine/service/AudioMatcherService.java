package com.example.audioengine.service;

import com.example.audioengine.model.AudioFile;
import com.example.audioengine.repository.AudioFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class AudioMatcherService {
    private static final Logger logger = LoggerFactory.getLogger(AudioMatcherService.class);
    
    @Autowired
    private AudioFileRepository audioFileRepo;

    public List<File> matchWordsToAudio(List<String> words) {
        logger.info("Matching {} words to audio files", words.size());
        List<File> matchedFiles = new ArrayList<>();
        
        for (String word : words) {
            logger.debug("Looking for audio file for word: '{}'", word);
            
            audioFileRepo.findByKeyword(word).ifPresentOrElse(
                audio -> {
                    File audioFile = new File("src/main/resources/audio/" + audio.getFilename());
                    if (audioFile.exists()) {
                        matchedFiles.add(audioFile);
                        logger.debug("Found audio file for '{}': {}", word, audioFile.getAbsolutePath());
                    } else {
                        logger.warn("Audio file not found on disk: {}", audioFile.getAbsolutePath());
                    }
                },
                () -> logger.debug("No audio file found in database for word: '{}'", word)
            );
        }
        
        logger.info("Matched {} audio files out of {} words", matchedFiles.size(), words.size());
        return matchedFiles;
    }
} 