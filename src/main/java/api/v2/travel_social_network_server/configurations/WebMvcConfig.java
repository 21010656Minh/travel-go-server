package api.v2.travel_social_network_server.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL /uploads/** tới thư mục cục bộ "uploads/"
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/"); // file: -> đọc từ file system
    }
}