package api.v2.travel_social_network_server.services.mail;

import api.v2.travel_social_network_server.dtos.mail.MailDto;
import jakarta.mail.MessagingException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IMailService {
    CompletableFuture<Void> sendMail(MailDto mailDto);
}
