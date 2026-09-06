package api.v2.travel_social_network_server.configurations;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.name}")
    String cloudName;

    @Value("${cloudinary.api.key}")
    String cloudApiKey;

    @Value("${cloudinary.api.secret}")
    String cloudApiSecret;

    @Bean
    public Cloudinary getCloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", cloudApiKey);
        config.put("api_secret", cloudApiSecret);
        config.put("secure", true);

        return new Cloudinary(config);
    }
}