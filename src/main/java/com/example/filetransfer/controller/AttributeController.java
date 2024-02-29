package com.example.filetransfer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/attributes")
public class AttributeController {

    @Value("${file.storage.location}")
    private String fileStorageLocation;

    @GetMapping("/**")
    public ResponseEntity<Map<String, Object>> getFileAttributes(HttpServletRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        try {
            String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            String decodedPath = URLDecoder.decode(fullPath, StandardCharsets.UTF_8.name());

            String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            AntPathMatcher matcher = new AntPathMatcher();
            String filePathString = matcher.extractPathWithinPattern(bestMatchPattern, decodedPath);

            Path filePath = Paths.get(fileStorageLocation, filePathString).normalize();
            if (!Files.exists(filePath)) {
                attributes.put("message", "File not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(attributes);
            }

            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

            LocalDateTime createdTime = LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime modifiedTime = LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());

            attributes.put("size", formatFileSize(attrs.size()));
            attributes.put("created", formatter.format(createdTime));
            attributes.put("modified", formatter.format(modifiedTime));

            // Bestimmen Sie den Dateityp basierend auf dem Dateinamen und fügen Sie ihn den Attributen hinzu
            String fileType = determineFileType(filePathString);
            attributes.put("type", fileType);

            return ResponseEntity.ok(attributes);
        } catch (IOException ex) {
            attributes.put("message", "Error getting file attributes: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(attributes);
        }
    }

    // Methode zur Bestimmung des Dateityps basierend auf dem Dateinamen

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        int exp = (int) (Math.log(size) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "B";
        return String.format("%.1f %s", size / Math.pow(1024, exp), pre);
    }
    private String determineFileType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return "PDF Document";
        } else if (fileName.endsWith(".txt")) {
            return "Text Document";
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return "Word Document";
        } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return "Excel Spreadsheet";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif")) {
            return "Image";
        } else if (Files.isDirectory(Paths.get(fileStorageLocation, fileName))) {
            return "Folder";
        } else {
            return "Unknown";
        }
    }
}
