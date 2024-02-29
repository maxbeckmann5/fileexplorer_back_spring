package com.example.filetransfer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${file.storage.location}")
    private String fileStorageLocation;

    // upload/uploaded-files/test.txt
    @PutMapping("/**")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("fileToUpload") MultipartFile file,
                                                          HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            // get full path of http requ (localhost:8080/upload/sub/folder/path/file.txt
            String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            // get path matching request mapping (/**) /sub/folder
            String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

            // create new matcher
            AntPathMatcher matcher = new AntPathMatcher();
            // extract sub/folder/path/file
            String directoryPath = matcher.extractPathWithinPattern(bestMatchPattern, fullPath);

            // resolve path from root and clean up
            // C:/projkete/filetransfer/uploaded-files/sub/folder/file.txt
            Path targetLocation = Paths.get(fileStorageLocation, directoryPath, file.getOriginalFilename()).normalize();

            // createDirectories creates complete path if !exist, createDirectory would throw error if path !exist
            // gets path out of sub/folder/path/file
            Files.createDirectories(targetLocation.getParent());

            // check if file exists
            if (Files.exists(targetLocation)) {
                response.put("message", "A file with the name '" + file.getOriginalFilename() + "' already exists.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // get file of http requ and make content into bytestream - all into path (incl filename)
            // overwrite if exists
            Files.copy(file.getInputStream(), targetLocation);

            // create response message
            response.put("message", "File uploaded successfully: " + file.getOriginalFilename());
            // return 200 + msg
            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            response.put("message", "Could not store file. Error: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}