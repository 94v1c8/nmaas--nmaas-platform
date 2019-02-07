package net.geant.nmaas.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
public class NotificationController {

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public NotificationController(ApplicationEventPublisher eventPublisher){
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public void sendMail(@RequestBody MailAttributes mailAttributes){
        eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
    }
}
