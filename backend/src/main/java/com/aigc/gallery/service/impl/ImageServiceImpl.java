package com.aigc.gallery.service.impl;

import com.aigc.gallery.model.ImageInfo;
import com.aigc.gallery.model.ScanDirectory;
import com.aigc.gallery.repository.ImageRepository;
import com.aigc.gallery.repository.ScanDirectoryRepository;
import com.aigc.gallery.service.ImageScanService;
import com.aigc.gallery.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ImageServiceImpl implements ImageService {
    
    @Autowired
    private ImageRepository imageRepository;
    
    @Autowired
    private ImageScanService imageScanService;
    
    @Autowired
    private ScanDirectoryRepository scanDirectoryRepository;
    
    private static final int THUMBNAIL_WIDTH = 300;
    private static final String IMAGES_DIR = "images";
    private static final String THUMBNAIL_DIR = "thumbnails";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public ImageServiceImpl() {
        // 创建必要的目录
        createRequiredDirectories();
    }
    
    private void createRequiredDirectories() {
        try {
            Files.createDirectories(Paths.get(IMAGES_DIR));
            Files.createDirectories(Paths.get(THUMBNAIL_DIR));
        } catch (IOException e) {
            log.error("Failed to create required directories", e);
            throw new RuntimeException("Failed to create required directories", e);
        }
    }
    
    @Override
    public void setBasePath(String path) {
        imageScanService.setBasePath(path);
    }
    
    @Override
    @Transactional
    public void scanAndUpdateImages() {
        List<ImageInfo> images = imageScanService.scanImages();
        for (ImageInfo image : images) {
            try {
                // 复制原始图片到应用管理的目录
                String newImagePath = copyImageToManagedDirectory(image.getFilePath());
                image.setFilePath("/images/" + Paths.get(newImagePath).getFileName());
                
                // 生成缩略图
                String thumbnailPath = generateThumbnail(newImagePath);
                if (thumbnailPath != null) {
                    image.setThumbnailPath("/thumbnails/" + Paths.get(thumbnailPath).getFileName());
                }
                
                // 保存或更新图片信息
                imageRepository.save(image);
            } catch (Exception e) {
                log.error("Error processing image: " + image.getFilePath(), e);
            }
        }
    }
    
    private String copyImageToManagedDirectory(String sourcePath) throws IOException {
        Path source = Paths.get(sourcePath);
        String fileName = source.getFileName().toString();
        Path targetPath = Paths.get(IMAGES_DIR, fileName);
        
        // 如果目标文件已存在，生成唯一文件名
        if (Files.exists(targetPath)) {
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            int counter = 1;
            
            do {
                fileName = baseName + "_" + counter + extension;
                targetPath = Paths.get(IMAGES_DIR, fileName);
                counter++;
            } while (Files.exists(targetPath));
        }
        
        // 复制文件
        Files.copy(source, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toString();
    }
    
    @Override
    public Map<String, List<ImageInfo>> getImagesByDate(Pageable pageable) {
        List<ImageInfo> images = imageRepository.findAll(pageable).getContent();
        return images.stream()
            .collect(Collectors.groupingBy(
                image -> image.getCreateTime().format(DATE_FORMATTER),
                TreeMap::new,
                Collectors.toList()
            ));
    }
    
    @Override
    public Map<String, List<ImageInfo>> getImagesByTag(Pageable pageable) {
        List<ImageInfo> images = imageRepository.findAll(pageable).getContent();
        Map<String, List<ImageInfo>> tagMap = new HashMap<>();
        
        for (ImageInfo image : images) {
            for (String tag : image.getTags()) {
                tagMap.computeIfAbsent(tag, k -> new ArrayList<>()).add(image);
            }
        }
        
        return new TreeMap<>(tagMap);
    }
    
    @Override
    public Map<String, List<ImageInfo>> getImagesByArtist(Pageable pageable) {
        List<ImageInfo> images = imageRepository.findAll(pageable).getContent();
        Map<String, List<ImageInfo>> artistMap = new HashMap<>();
        
        for (ImageInfo image : images) {
            for (String artist : image.getArtists()) {
                artistMap.computeIfAbsent(artist, k -> new ArrayList<>()).add(image);
            }
        }
        
        return new TreeMap<>(artistMap);
    }
    
    @Override
    public Set<String> getAllTags() {
        return imageRepository.findAllTags();
    }
    
    @Override
    public Set<String> getAllArtists() {
        return imageRepository.findAllArtists();
    }
    
    @Override
    public Page<ImageInfo> searchByTags(Set<String> tags, Pageable pageable) {
        return imageRepository.findByTagsIn(tags, pageable);
    }
    
    @Override
    public ImageInfo getImageDetail(Long id) {
        return imageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Image not found"));
    }
    
    @Override
    public String generateThumbnail(String imagePath) {
        try {
            File sourceFile = new File(imagePath);
            BufferedImage sourceImage = ImageIO.read(sourceFile);
            
            // 计算缩略图尺寸
            int thumbnailHeight = (int) ((double) THUMBNAIL_WIDTH / sourceImage.getWidth() * sourceImage.getHeight());
            
            // 创建缩略图
            BufferedImage thumbnail = new BufferedImage(THUMBNAIL_WIDTH, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(sourceImage, 0, 0, THUMBNAIL_WIDTH, thumbnailHeight, null);
            g2d.dispose();
            
            // 生成缩略图文件名
            String thumbnailName = "thumb_" + sourceFile.getName();
            File thumbnailFile = new File(THUMBNAIL_DIR, thumbnailName);
            
            // 保存缩略图
            ImageIO.write(thumbnail, "JPEG", thumbnailFile);
            
            return thumbnailFile.getPath();
        } catch (Exception e) {
            log.error("Error generating thumbnail for: " + imagePath, e);
            return null;
        }
    }
    
    @Override
    public Long addScanDirectory(String path, String description) {
        // 检查路径是否已存在
        if (scanDirectoryRepository.existsByPath(path)) {
            return null;
        }
        
        // 创建新的扫描目录记录
        ScanDirectory directory = new ScanDirectory(path);
        directory.setDescription(description);
        directory = scanDirectoryRepository.save(directory);
        
        // 立即扫描新添加的目录
        try {
            imageScanService.setBasePath(path);
            List<ImageInfo> newImages = imageScanService.scanImages();
            for (ImageInfo image : newImages) {
                // 复制图片到管理目录并保存
                processAndSaveImage(image);
            }
            
            // 更新目录信息
            directory.setImageCount(newImages.size());
            directory.setLastScanTime(LocalDateTime.now());
            scanDirectoryRepository.save(directory);
            
            return directory.getId();
        } catch (Exception e) {
            log.error("Failed to scan directory: " + path, e);
            // 删除目录记录
            scanDirectoryRepository.delete(directory);
            return null;
        }
    }
    
    @Override
    public boolean removeScanDirectory(Long directoryId) {
        Optional<ScanDirectory> directoryOpt = scanDirectoryRepository.findById(directoryId);
        if (directoryOpt.isEmpty()) {
            return false;
        }
        
        // 删除目录记录（不删除实际文件）
        scanDirectoryRepository.deleteById(directoryId);
        return true;
    }
    
    @Override
    public int rescanDirectory(Long directoryId) {
        Optional<ScanDirectory> directoryOpt = scanDirectoryRepository.findById(directoryId);
        if (directoryOpt.isEmpty()) {
            throw new IllegalArgumentException("Directory not found");
        }
        
        ScanDirectory directory = directoryOpt.get();
        imageScanService.setBasePath(directory.getPath());
        List<ImageInfo> scannedImages = imageScanService.scanImages();
        int updatedCount = 0;
        
        for (ImageInfo newImage : scannedImages) {
            // 检查图片是否已存在
            Optional<ImageInfo> existingImage = imageRepository.findByFilePath(newImage.getFilePath());
            if (existingImage.isPresent()) {
                // 更新现有图片的标签等信息
                ImageInfo image = existingImage.get();
                image.setTags(newImage.getTags());
                image.setArtists(newImage.getArtists());
                image.setPrompt(newImage.getPrompt());
                image.setNegativePrompt(newImage.getNegativePrompt());
                image.setMetadata(newImage.getMetadata());
                imageRepository.save(image);
                updatedCount++;
            } else {
                // 处理新图片
                processAndSaveImage(newImage);
                updatedCount++;
            }
        }
        
        // 更新目录信息
        directory.setImageCount(scannedImages.size());
        directory.setLastScanTime(LocalDateTime.now());
        scanDirectoryRepository.save(directory);
        
        return updatedCount;
    }
    
    @Override
    public List<ScanDirectory> getAllScanDirectories() {
        return scanDirectoryRepository.findAll();
    }
    
    /**
     * 处理并保存图片
     */
    private void processAndSaveImage(ImageInfo image) {
        try {
            // 复制原始图片到应用管理的目录
            String newImagePath = copyImageToManagedDirectory(image.getFilePath());
            image.setFilePath("/images/" + Paths.get(newImagePath).getFileName());
            
            // 生成缩略图
            String thumbnailPath = generateThumbnail(newImagePath);
            if (thumbnailPath != null) {
                image.setThumbnailPath("/thumbnails/" + Paths.get(thumbnailPath).getFileName());
            }
            
            // 保存图片信息
            imageRepository.save(image);
        } catch (Exception e) {
            log.error("Failed to process and save image: " + image.getFilePath(), e);
        }
    }
} 