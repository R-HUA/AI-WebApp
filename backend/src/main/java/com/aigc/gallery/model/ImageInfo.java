package com.aigc.gallery.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "image_info")
public class ImageInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fileName;
    private String filePath;
    private String relativePath;
    private LocalDateTime createTime;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String prompt;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String negativePrompt;
    
    private Long fileSize;
    private String thumbnailPath;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "image_tags", joinColumns = @JoinColumn(name = "image_id"))
    @Column(name = "tag")
    private Set<String> tags;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "image_artists", joinColumns = @JoinColumn(name = "image_id"))
    @Column(name = "artist")
    private Set<String> artists;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String metadata; // 存储完整的元数据JSON
} 