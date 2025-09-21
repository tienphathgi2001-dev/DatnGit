package com.asm5.config; 

import com.asm5.interceptor.AdminInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**") // Áp dụng cho tất cả URL
                .excludePathPatterns("/taikhoan", "/login", "/logout", "/access-denied"); // Loại trừ các trang công khai
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ảnh tĩnh trong resources/static/img
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");

        // Ảnh upload động lưu ngoài project (ví dụ trong D:/uploads/)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/uploads/");
    }


}
