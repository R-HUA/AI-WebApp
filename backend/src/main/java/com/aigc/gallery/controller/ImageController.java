package com.aigc.gallery.controller;

import com.aigc.gallery.model.ImageInfo;
import com.aigc.gallery.model.ScanDirectory;
import com.aigc.gallery.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/images")
@CrossOrigin
public class ImageController {
    
    @Autowired
    private ImageService imageService;
    
    @PostMapping("/scan")
    public ResponseEntity<Void> scanImages(@RequestParam String basePath) {
        imageService.setBasePath(basePath);
        imageService.scanAndUpdateImages();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/by-date")
    public ResponseEntity<Map<String, List<ImageInfo>>> getImagesByDate(Pageable pageable) {
        return ResponseEntity.ok(imageService.getImagesByDate(pageable));
    }
    
    @GetMapping("/by-tag")
    public ResponseEntity<Map<String, List<ImageInfo>>> getImagesByTag(Pageable pageable) {
        return ResponseEntity.ok(imageService.getImagesByTag(pageable));
    }
    
    @GetMapping("/by-artist")
    public ResponseEntity<Map<String, List<ImageInfo>>> getImagesByArtist(Pageable pageable) {
        return ResponseEntity.ok(imageService.getImagesByArtist(pageable));
    }
    
    @GetMapping("/tags")
    public ResponseEntity<Set<String>> getAllTags() {
        return ResponseEntity.ok(imageService.getAllTags());
    }
    
    @GetMapping("/artists")
    public ResponseEntity<Set<String>> getAllArtists() {
        return ResponseEntity.ok(imageService.getAllArtists());
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<ImageInfo>> searchByTags(
            @RequestParam Set<String> tags,
            Pageable pageable) {
        return ResponseEntity.ok(imageService.searchByTags(tags, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ImageInfo> getImageDetail(@PathVariable Long id) {
        return ResponseEntity.ok(imageService.getImageDetail(id));
    }
    
    /**
     * 通过ID获取图片文件
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getImageFile(@PathVariable Long id) {
        try {
            ImageInfo imageInfo = imageService.getImageDetail(id);
            if (imageInfo == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 去掉路径前缀 "/images/"
            String fileName = imageInfo.getFilePath().replace("/images/", "");
            Path filePath = Paths.get("images", fileName);
            Resource resource = new FileSystemResource(filePath.toFile());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.IMAGE_PNG) // 或根据实际图片类型设置
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取缩略图文件
     */
    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> getThumbnailFile(@PathVariable Long id) {
        try {
            ImageInfo imageInfo = imageService.getImageDetail(id);
            if (imageInfo == null || imageInfo.getThumbnailPath() == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 去掉路径前缀 "/thumbnails/"
            String fileName = imageInfo.getThumbnailPath().replace("/thumbnails/", "");
            Path filePath = Paths.get("thumbnails", fileName);
            Resource resource = new FileSystemResource(filePath.toFile());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.IMAGE_PNG) // 或根据实际图片类型设置
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 添加扫描目录
     */
    @PostMapping("/directories")
    public ResponseEntity<?> addScanDirectory(
            @RequestParam String path,
            @RequestParam(required = false) String description) {
        Long directoryId = imageService.addScanDirectory(path, description);
        if (directoryId == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Directory already exists"));
        }
        return ResponseEntity.ok(Map.of("id", directoryId));
    }
    
    /**
     * 获取所有扫描目录
     */
    @GetMapping("/directories")
    public ResponseEntity<List<ScanDirectory>> getAllDirectories() {
        return ResponseEntity.ok(imageService.getAllScanDirectories());
    }
    
    /**
     * 删除扫描目录
     */
    @DeleteMapping("/directories/{id}")
    public ResponseEntity<?> removeDirectory(@PathVariable Long id) {
        boolean success = imageService.removeScanDirectory(id);
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
    
    /**
     * 重新扫描指定目录
     */
    @PostMapping("/directories/{id}/rescan")
    public ResponseEntity<?> rescanDirectory(@PathVariable Long id) {
        try {
            int updatedCount = imageService.rescanDirectory(id);
            return ResponseEntity.ok(Map.of(
                "message", "Directory rescanned successfully",
                "updatedCount", updatedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
} 