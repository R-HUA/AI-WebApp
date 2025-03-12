package com.aigc.gallery.service;

import com.aigc.gallery.model.ImageInfo;
import com.aigc.gallery.util.TagProcessor;
import com.aigc.gallery.util.PromptExtractor;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.png.PngDirectory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ImageScanService {
    private static final Pattern DATE_PATTERN_1 = Pattern.compile("^(\\d{8}).*");
    private static final Pattern DATE_PATTERN_2 = Pattern.compile("^(\\d{2}-\\d{2}-\\d{2}).*");
    
    @Autowired
    private TagProcessor tagProcessor;
    
    @Autowired
    private PromptExtractor promptExtractor;
    
    private Path basePath;
    
    public void setBasePath(String path) {
        this.basePath = Paths.get(path).toAbsolutePath().normalize();
    }
    
    /**
     * 扫描指定目录下的所有图片
     */
    public List<ImageInfo> scanImages() {
        if (basePath == null) {
            throw new IllegalStateException("Base path not set");
        }
        
        // 检查目录是否存在
        if (!Files.exists(basePath)) {
            throw new IllegalArgumentException("Directory does not exist: " + basePath);
        }
        
        // 检查是否是目录
        if (!Files.isDirectory(basePath)) {
            throw new IllegalArgumentException("Path is not a directory: " + basePath);
        }
        
        List<ImageInfo> images = new ArrayList<>();
        try {
            Files.find(basePath, 
                       Integer.MAX_VALUE,
                       (path, attrs) -> {
                           // 只处理常规文件，跳过目录和特殊文件
                           if (!attrs.isRegularFile()) {
                               return false;
                           }
                           // 检查是否为图片文件
                           return isImageFile(path.toString());
                       })
                .forEach(path -> {
                    try {
                        ImageInfo imageInfo = processImageFile(path);
                        if (imageInfo != null) {
                            images.add(imageInfo);
                        }
                    } catch (Exception e) {
                        log.error("Error processing image: " + path, e);
                    }
                });
        } catch (IOException e) {
            log.error("Error scanning directory: " + basePath, e);
            throw new RuntimeException("Failed to scan directory: " + basePath, e);
        }
        
        return images;
    }
    
    /**
     * 处理单个图片文件
     */
    private ImageInfo processImageFile(Path path) {
        try {
            File file = path.toFile();
            ImageInfo imageInfo = new ImageInfo();
            imageInfo.setFileName(file.getName());
            imageInfo.setFilePath(file.getAbsolutePath());
            imageInfo.setRelativePath(basePath.relativize(path).toString());
            imageInfo.setFileSize(file.length());
            
            // 解析文件名中的日期
            LocalDateTime createTime = parseCreateTimeFromFileName(file.getName());
            imageInfo.setCreateTime(createTime != null ? createTime : 
                LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), 
                    TimeZone.getDefault().toZoneId()));
            
            // 读取图片元数据
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            processImageMetadata(metadata, imageInfo);
            
            return imageInfo;
        } catch (Exception e) {
            log.error("Error processing file: " + path, e);
            return null;
        }
    }
    
    /**
     * 处理图片元数据
     */
    private void processImageMetadata(Metadata metadata, ImageInfo imageInfo) {
        for (Directory directory : metadata.getDirectories()) {
            if (directory instanceof PngDirectory) {
                String description = directory.getString(PngDirectory.TAG_TEXTUAL_DATA);
                if (description != null) {
                    // 处理 NovelAI 和 ComfyUI 的元数据格式
                    processPromptMetadata(description, imageInfo);
                }
            }
        }
    }
    
    /**
     * 处理提示词元数据
     */
    private void processPromptMetadata(String metadata, ImageInfo imageInfo) {
        // 存储原始元数据
        imageInfo.setMetadata(metadata);
        
        // 提取提示词
        String[] prompts = promptExtractor.extractPrompts(metadata);
        imageInfo.setPrompt(prompts[0]);
        imageInfo.setNegativePrompt(prompts[1]);
        
        // 处理标签
        Set<String> tags = tagProcessor.extractTags(prompts[0]);
        Set<String> artists = tagProcessor.extractArtists(tags);
        
        imageInfo.setTags(tags);
        imageInfo.setArtists(artists);
    }
    
    /**
     * 从文件名解析创建时间
     */
    private LocalDateTime parseCreateTimeFromFileName(String fileName) {
        Matcher matcher1 = DATE_PATTERN_1.matcher(fileName);
        if (matcher1.matches()) {
            String dateStr = matcher1.group(1);
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        
        Matcher matcher2 = DATE_PATTERN_2.matcher(fileName);
        if (matcher2.matches()) {
            String dateStr = matcher2.group(1);
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yy-MM-dd"));
        }
        
        return null;
    }
    
    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String path) {
        String lowercasePath = path.toLowerCase();
        return lowercasePath.endsWith(".png") || 
               lowercasePath.endsWith(".jpg") || 
               lowercasePath.endsWith(".jpeg");
    }
} 