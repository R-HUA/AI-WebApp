package com.aigc.gallery.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片扫描目录实体类
 */
@Entity
@Data
public class ScanDirectory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 目录路径
    private String path;
    
    // 上次扫描时间
    private LocalDateTime lastScanTime;
    
    // 目录描述（可选）
    private String description;
    
    // 目录包含的图片数量
    private Integer imageCount;
    
    // 添加时间
    private LocalDateTime createdTime;
    
    public ScanDirectory() {
        this.createdTime = LocalDateTime.now();
        this.imageCount = 0;
    }
    
    public ScanDirectory(String path) {
        this();
        this.path = path;
    }
} 