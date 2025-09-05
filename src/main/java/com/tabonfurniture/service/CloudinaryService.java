package com.tabonfurniture.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Check if Cloudinary servers are reachable
     */
    public boolean isCloudinaryReachable() {
        try {
            InetAddress.getByName("api.cloudinary.com");
            return true;
        } catch (UnknownHostException e) {
            System.err.println("❌ Cannot resolve api.cloudinary.com: " + e.getMessage());
            return false;
        }
    }

    /**
     * Test Cloudinary connection
     */
    public String testConnection() {
        if (!isCloudinaryReachable()) {
            return "Network error: Cannot reach Cloudinary servers. Please check your internet connection.";
        }
        
        try {
            // Try a simple API call to test connection
            cloudinary.api().ping(ObjectUtils.emptyMap());
            return "Connection successful";
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
    }

    public String uploadImage(MultipartFile file) throws IOException {
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return (String) uploadResult.get("secure_url");
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap("folder", folder);
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) uploadResult.get("secure_url");
    }
    
    public String uploadVideo(MultipartFile file) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap("resource_type", "video");
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) uploadResult.get("secure_url");
    }
    
    public String uploadVideo(MultipartFile file, String folder) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap("resource_type", "video", "folder", folder);
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) uploadResult.get("secure_url");
    }
    
    public String uploadPdf(MultipartFile file) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap("resource_type", "raw");
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) uploadResult.get("secure_url");
    }
    
    public String uploadPdf(MultipartFile file, String folder) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap("resource_type", "raw", "folder", folder);
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) uploadResult.get("secure_url");
    }

    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
    
    public void deletePdf(String publicId) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap("resource_type", "raw");
        cloudinary.uploader().destroy(publicId, options);
    }

    public String getPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // Extract public ID from Cloudinary URL
        // Example: https://res.cloudinary.com/djyloeohk/image/upload/v1234567890/folder/image.jpg
        String[] parts = url.split("/");
        if (parts.length >= 8) {
            // Find the upload part and get everything after it
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i]) && i + 2 < parts.length) {
                    StringBuilder publicId = new StringBuilder();
                    for (int j = i + 2; j < parts.length; j++) {
                        if (j > i + 2) {
                            publicId.append("/");
                        }
                        publicId.append(parts[j]);
                    }
                    // Remove file extension
                    String result = publicId.toString();
                    int lastDot = result.lastIndexOf('.');
                    if (lastDot > 0) {
                        result = result.substring(0, lastDot);
                    }
                    return result;
                }
            }
        }
        return null;
    }
} 