package com.example.filetransfer.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.http.HttpStatus;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/delete")
public class DeleteController {

    @Value("${file.storage.location}")
    private String fileStorageLocation;

    @DeleteMapping("/**")
    public ResponseEntity<Map<String, String>> deleteFile(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

            // Vor URL-Decoder
            System.out.println("Full Path vor URL-Decoder: " + fullPath);
            String decodedPath = URLDecoder.decode(fullPath, StandardCharsets.UTF_8.name());
            // Nach URL-Decoder
            System.out.println("Decodierter Pfad: " + decodedPath);

            String filePathString = decodedPath.substring("/delete/".length());

            Path filePath = Paths.get(fileStorageLocation, filePathString).normalize();
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                response.put("message", "File not found: " + filePathString);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Files.delete(filePath);

            response.put("message", "File deleted successfully: " + filePathString);
            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            response.put("message", "Could not delete file. Error: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
