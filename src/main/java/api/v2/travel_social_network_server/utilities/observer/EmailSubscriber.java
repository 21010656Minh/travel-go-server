package api.v2.travel_social_network_server.utilities.observer;

import api.v2.travel_social_network_server.dtos.mail.MailDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.services.mail.IMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component("emailSubscriber")
@RequiredArgsConstructor
@Service
@Slf4j
public class EmailSubscriber implements Subscriber<User> {
    
    private final IMailService mailService;
    
    @Override
    @Async("taskExecutor")
    public void onMessage(User user) {
        log.info("Sending password change email to user: {}", user.getUserId());
        
        String currentTime = Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("userName", user.getUserProfile().getFullName());
        placeholders.put("changeTime", currentTime);
        placeholders.put("userEmail", user.getEmail());
        
        MailDto mailDto = MailDto.builder()
                .to(user.getEmail())
                .subject("Password Change Notification - GoWay")
                .templateName("password_change_template")
                .placeholders(placeholders)
                .build();
        
        mailService.sendMail(mailDto);
        
        log.info("Password change email sent to user: {}", user.getUserId());
    }
}
