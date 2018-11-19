package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.model.ConfirmationEmail;
import net.geant.nmaas.portal.api.model.FailureEmail;
import net.geant.nmaas.portal.auth.basic.TokenAuthenticationService;
import net.geant.nmaas.portal.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Value("${notification.url}")
    private String url;

    @Value("${notification.port}")
    private String port;

    @Value("${notification.path}")
    private String emailConfirmationPath;

    @Value("${notification.error.path}")
    private String failureEmailPath;

    private TokenAuthenticationService tokenAuthenticationService;

    @Autowired
    public NotificationServiceImpl(TokenAuthenticationService tokenAuthenticationService){
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Override
    public void sendEmail(ConfirmationEmail confirmationEmail) {
        final String uri = String.format("%s:%s%s", url, port, emailConfirmationPath);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + tokenAuthenticationService.getAnonymousAccessToken());
        HttpEntity<ConfirmationEmail> entity = new HttpEntity<>(confirmationEmail, headers);

        restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
    }

    @Override
    public void sendFailureEmail(FailureEmail emailConfirmation) {
        final String uri = String.format("%s:%s%s", url, port, failureEmailPath);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + tokenAuthenticationService.getAnonymousAccessToken());
        HttpEntity<FailureEmail> entity = new HttpEntity<>(emailConfirmation, headers);

        restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
    }
}
