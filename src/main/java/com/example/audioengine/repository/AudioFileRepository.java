package com.example.audioengine.repository;

import com.example.audioengine.model.AudioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AudioFileRepository extends JpaRepository<AudioFile, Long> {
    Optional<AudioFile> findByKeyword(String keyword);
} 