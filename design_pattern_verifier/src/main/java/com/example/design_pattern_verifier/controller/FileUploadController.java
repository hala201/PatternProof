package com.example.design_pattern_verifier.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.design_pattern_verifier.service.AnalyzeService;

@RestController
public class FileUploadController {
    private final Path root = Paths.get("uploads");

    @Autowired
    private AnalyzeService analyzeService;

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("files") MultipartFile[] files, @RequestParam("pattern") String pattern) {
        if (files.length == 0) {
            return ResponseEntity.badRequest().body("No files provided!");
        }

        String uniqueDirName = "session_" + System.currentTimeMillis();
        Path sessionDir = this.root.resolve(uniqueDirName);

        try {
            Files.createDirectories(sessionDir);
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String filename = Paths.get(file.getOriginalFilename()).getFileName().toString();
                    Path destinationPath = sessionDir.resolve(filename).normalize().toAbsolutePath();
                    if (!destinationPath.getParent().equals(sessionDir.toAbsolutePath())) {
                        throw new SecurityException("Cannot store file outside the session directory.");
                    }
                    try (InputStream inputStream = file.getInputStream()) {
                        Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    return ResponseEntity.badRequest().body("Empty file in the request.");
                }
            }
            return ResponseEntity.ok(this.analyzeService.analyseSourceDirectory(sessionDir.toString(), pattern));
        } catch (Exception e) {
            System.err.println("Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload due to server error.");
        }
    }
}
