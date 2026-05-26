package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Đường dẫn trỏ thẳng vào thư mục chứa ảnh thực tế
        Path uploadDir = Paths.get("uploads/images");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // Hỗ trợ cả 2 định dạng URL cấu hình để giao diện cũ hay mới đều đọc được ảnh
        registry.addResourceHandler("/uploads/images/**", "/images/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}