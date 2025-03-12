package com.aigc.gallery.repository;

import com.aigc.gallery.model.ImageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageInfo, Long> {
    
    @Query("SELECT DISTINCT t FROM ImageInfo i JOIN i.tags t")
    Set<String> findAllTags();
    
    @Query("SELECT DISTINCT a FROM ImageInfo i JOIN i.artists a")
    Set<String> findAllArtists();
    
    Page<ImageInfo> findByTagsIn(Set<String> tags, Pageable pageable);

    /**
     * 根据文件路径查找图片
     */
    Optional<ImageInfo> findByFilePath(String filePath);
} 