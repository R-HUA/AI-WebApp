package com.aigc.gallery.repository;

import com.aigc.gallery.model.ScanDirectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScanDirectoryRepository extends JpaRepository<ScanDirectory, Long> {
    
    /**
     * 根据路径查找目录
     */
    Optional<ScanDirectory> findByPath(String path);
    
    /**
     * 检查路径是否已存在
     */
    boolean existsByPath(String path);
} 