package com.aigc.gallery.util;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TagProcessor {
    private static final String ARTIST_PREFIX = "artist:";
    private static final Pattern WEIGHT_PATTERN = Pattern.compile("^[\\[{(].*[\\]})]$");
    private static final Pattern BALANCED_PARENTHESES = Pattern.compile("^\\(.*\\)$");
    
    /**
     * 处理原始提示词，提取标签
     */
    public Set<String> extractTags(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<String> tags = new HashSet<>();
        String[] rawTags = prompt.split(",");
        
        for (String tag : rawTags) {
            String cleanTag = cleanTag(tag.trim());
            if (!cleanTag.isEmpty()) {
                tags.add(cleanTag);
            }
        }
        
        return tags;
    }
    
    /**
     * 提取画师标签
     */
    public Set<String> extractArtists(Set<String> tags) {
        Set<String> artists = new HashSet<>();
        Iterator<String> iterator = tags.iterator();
        
        while (iterator.hasNext()) {
            String tag = iterator.next();
            if (tag.toLowerCase().startsWith(ARTIST_PREFIX)) {
                String artist = tag.substring(ARTIST_PREFIX.length()).trim();
                if (!artist.isEmpty()) {
                    artists.add(artist);
                    iterator.remove(); // 从普通标签中移除画师标签
                }
            }
        }
        
        return artists;
    }
    
    /**
     * 清理标签中的权重标记
     */
    private String cleanTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return "";
        }
        
        tag = tag.trim();
        
        // 处理方括号和花括号
        if (tag.startsWith("[") && tag.endsWith("]") ||
            tag.startsWith("{") && tag.endsWith("}")) {
            tag = tag.substring(1, tag.length() - 1);
        }
        
        // 处理圆括号，只有在开头和结尾都有时才移除
        if (BALANCED_PARENTHESES.matcher(tag).matches()) {
            tag = tag.substring(1, tag.length() - 1);
        }
        
        // 移除权重数字
        tag = tag.replaceAll(":\\d+(\\.\\d+)?", "");
        
        return tag.trim();
    }
    
    /**
     * 判断是否为权重标记
     */
    private boolean isWeightTag(String tag) {
        return WEIGHT_PATTERN.matcher(tag).matches();
    }
} 