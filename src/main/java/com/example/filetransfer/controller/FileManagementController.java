package com.example.filetransfer.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import com.example.filetransfer.ErrorResponse;
import com.example.filetransfer.FileInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file-management")
public class FileManagementController {

    @Value("${file.storage.location}")
    private String fileStorageLocation;

    @GetMapping("/list-files/**")
    public ResponseEntity<?> listFilesInDirectory(HttpServletRequest request) {
        try {
            String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            AntPathMatcher matcher = new AntPathMatcher();
            String directoryPath = matcher.extractPathWithinPattern("/file-management/list-files/**", fullPath);

            Path dirPath = Paths.get(fileStorageLocation, directoryPath).normalize();

            List<FileInfo> fileList = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
                for (Path file : stream) {
                    boolean isDirectory = Files.isDirectory(file);
                    fileList.add(new FileInfo(file.getFileName().toString(), isDirectory));
                }
            }

            return ResponseEntity.ok(fileList);
        } catch (IOException e) {
            ErrorResponse errorResponse = new ErrorResponse("Error reading directory: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
