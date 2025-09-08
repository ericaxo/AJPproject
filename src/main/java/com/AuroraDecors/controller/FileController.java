package com.AuroraDecors.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/files")
public class FileController {

    @GetMapping("/cv/{filename:.+}")
    public ResponseEntity<Resource> downloadCV(@PathVariable String filename) {
        try {
            // Check if this is a Cloudinary URL (legacy data)
            if (filename.startsWith("http")) {
                // Redirect to Cloudinary URL for legacy CVs
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, filename)
                    .build();
            }
            
            // For local files, construct the path to the CV file
            Path filePath = Paths.get("uploads/cv/").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Set proper headers for PDF download
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"cv.pdf\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
