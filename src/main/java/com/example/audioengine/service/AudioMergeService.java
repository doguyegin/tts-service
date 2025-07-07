package com.example.audioengine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AudioMergeService {
    private static final Logger logger = LoggerFactory.getLogger(AudioMergeService.class);
    
    public File mergeAudioFiles(List<File> audioFiles) throws IOException {
        logger.info("Starting audio merge for {} files", audioFiles.size());
        
        if (audioFiles.isEmpty()) {
            logger.warn("No audio files provided for merging");
            throw new IOException("No audio files to merge");
        }
        
        File output = new File("output/merged.wav");
        output.getParentFile().mkdirs(); // Ensure output directory exists
        
        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        
        for (File file : audioFiles) {
            cmd.add("-i");
            cmd.add(file.getAbsolutePath());
            logger.debug("Added input file: {}", file.getAbsolutePath());
        }
        
        cmd.add("-filter_complex");
        StringBuilder filter = new StringBuilder();
        for (int i = 0; i < audioFiles.size(); i++) {
            filter.append("[" + i + ":0]");
        }
        filter.append("concat=n=" + audioFiles.size() + ":v=0:a=1[out]");
        cmd.add(filter.toString());
        cmd.add("-map");
        cmd.add("[out]");
        cmd.add(output.getAbsolutePath());

        logger.info("Executing ffmpeg command: {}", String.join(" ", cmd));
        
        try {
            Process process = new ProcessBuilder(cmd).start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                logger.info("Audio merge completed successfully. Output: {}", output.getAbsolutePath());
                return output;
            } else {
                logger.error("ffmpeg process failed with exit code: {}", exitCode);
                throw new IOException("ffmpeg process failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            logger.error("ffmpeg process was interrupted", e);
            Thread.currentThread().interrupt();
            throw new IOException("ffmpeg process was interrupted", e);
        } catch (IOException e) {
            logger.error("Failed to execute ffmpeg command", e);
            throw e;
        }
    }
} 