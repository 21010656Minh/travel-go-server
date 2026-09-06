package api.v2.travel_social_network_server;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TravelSocialNetworkServerApplication {
    public static void main(String[] args) {
        // Load .env file into system environment variables
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        SpringApplication.run(TravelSocialNetworkServerApplication.class, args);
    }
}
