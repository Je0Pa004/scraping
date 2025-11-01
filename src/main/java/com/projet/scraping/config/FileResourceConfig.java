package com.projet.scraping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    @Value("${app.cv.storageDir:uploads}")
    private String storageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + (storageDir == null || storageDir.isBlank() ? "uploads" : storageDir) + "/";
        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }
}
