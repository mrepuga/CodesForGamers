package com.example.user.controllers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ImageController {

    @Value("${upload.directory}")
    private String uploadDirectory;

    @GetMapping("/images/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        try {
            // Assuming your images are stored in the 'static/images' directory
            Path folderPath = Paths.get(uploadDirectory+ "/"+imageName);

            Resource resource = new UrlResource(folderPath.toUri());

            // Check if the resource exists
            if (resource.exists()) {
                // Set Cache-Control header to prevent caching
                return ResponseEntity.ok()
                        .cacheControl(CacheControl.noCache())
                        .body(resource);
            } else {
                // Return a 404 Not Found response if the image doesn't exist
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            // Handle exceptions (e.g., file not found)
            return ResponseEntity.status(500).build();
        }
    }


}