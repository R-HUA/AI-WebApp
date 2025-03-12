package com.aigc.gallery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置图片文件的访问路径
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:./images/");
                
        // 配置缩略图的访问路径
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:./thumbnails/");
    }
} 