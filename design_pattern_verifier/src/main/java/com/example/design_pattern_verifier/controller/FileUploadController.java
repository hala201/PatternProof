package com.example.design_pattern_verifier.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.design_pattern_verifier.visitor.MethodNamePrinter;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;


@RestController
public class FileUploadController {

    private final Path root = Paths.get("uploads");

    private void parseFilesWithJavaParser(Path destinationPath) throws IOException {
        ParserConfiguration configuration = new ParserConfiguration();
        JavaParser javaParser = new JavaParser(configuration);

        CompilationUnit compilationUnit;
        try (InputStream inputStream = Files.newInputStream(destinationPath)) {
            compilationUnit = javaParser.parse(inputStream).getResult().orElseThrow(() -> new IOException("Could not parse the file."));
        }

        MethodNamePrinter methodNamePrinter = new MethodNamePrinter();

        methodNamePrinter.visit(compilationUnit, null);
    }


    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("files") MultipartFile[] files) {
        if (files.length == 0) {
            return ResponseEntity.badRequest().body("No files provided!");
        }
        try {
            Files.createDirectories(this.root);
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String filename = Paths.get(file.getOriginalFilename()).getFileName().toString();
                    Path destinationPath = this.root.resolve(filename).normalize().toAbsolutePath();
                    if (!destinationPath.getParent().equals(this.root.toAbsolutePath())) {
                        throw new IOException("Cannot store file outside current directory.");
                    }
                    try (InputStream inputStream = file.getInputStream()) {
                        Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    this.parseFilesWithJavaParser(destinationPath);
                } else {
                    return ResponseEntity.badRequest().body("Empty file in the request.");
                }
            }
            return ResponseEntity.ok("File upload successful!");
        } catch (IOException e) {
            System.err.println("Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload due to server error.");
        }
    }
}

