package com.aigc.gallery.service;

import com.aigc.gallery.model.ImageInfo;
import com.aigc.gallery.model.ScanDirectory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ImageService {
    /**
     * 设置图片扫描基础路径
     */
    void setBasePath(String path);
    
    /**
     * 扫描并更新图片库
     */
    void scanAndUpdateImages();
    
    /**
     * 添加扫描目录
     * @return 添加成功返回目录ID，已存在则返回null
     */
    Long addScanDirectory(String path, String description);
    
    /**
     * 删除扫描目录
     * @return 删除成功返回true，不存在返回false
     */
    boolean removeScanDirectory(Long directoryId);
    
    /**
     * 重新扫描指定目录
     * @return 更新的图片数量
     */
    int rescanDirectory(Long directoryId);
    
    /**
     * 获取所有扫描目录
     */
    List<ScanDirectory> getAllScanDirectories();
    
    /**
     * 按日期分组获取图片
     */
    Map<String, List<ImageInfo>> getImagesByDate(Pageable pageable);
    
    /**
     * 按标签分组获取图片
     */
    Map<String, List<ImageInfo>> getImagesByTag(Pageable pageable);
    
    /**
     * 按画师分组获取图片
     */
    Map<String, List<ImageInfo>> getImagesByArtist(Pageable pageable);
    
    /**
     * 获取所有标签
     */
    Set<String> getAllTags();
    
    /**
     * 获取所有画师
     */
    Set<String> getAllArtists();
    
    /**
     * 根据标签搜索图片
     */
    Page<ImageInfo> searchByTags(Set<String> tags, Pageable pageable);
    
    /**
     * 获取图片详情
     */
    ImageInfo getImageDetail(Long id);
    
    /**
     * 生成图片缩略图
     */
    String generateThumbnail(String imagePath);
} 