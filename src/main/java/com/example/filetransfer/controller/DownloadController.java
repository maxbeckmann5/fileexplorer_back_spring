package com.example.filetransfer.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.util.AntPathMatcher;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;



@RestController
@RequestMapping("/download")
public class DownloadController {

    @Value("${file.storage.location}")
    private String fileStorageLocation;

    // download/root download/folder1/folder2/folder3
    @GetMapping("/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        try {
            String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            String decodedPath = URLDecoder.decode(fullPath, StandardCharsets.UTF_8.name());

            String filePathString = decodedPath.substring("/download/".length());

            Path filePath = Paths.get(fileStorageLocation, filePathString).normalize();
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new ByteArrayResource(Files.readAllBytes(filePath));
            String filename = filePath.getFileName().toString();

            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
