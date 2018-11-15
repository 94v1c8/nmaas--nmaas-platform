package net.geant.nmaas.portal.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailConfirmation extends Email{

    private String lastName;
    private String userName;

    @Builder
    public EmailConfirmation(String toEmail, String subject, String templateName, String firstName, String lastName, String userName){
        super(toEmail, subject, templateName, firstName);
        this.lastName = lastName;
        this.userName = userName;
    }
}
