package com.microservice.mediaservice.controller;

import com.microservice.mediaservice.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaStorageService mediaStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("username") String username) { // Require username from frontend
        String filename = mediaStorageService.uploadFile(file, username);
        return ResponseEntity.ok(filename);
    }

    @GetMapping("/url/{filename}")
    public ResponseEntity<String> getFileUrl(@PathVariable String filename) {
        String url = mediaStorageService.getFileUrl(filename);
        return ResponseEntity.ok(url);
    }
}