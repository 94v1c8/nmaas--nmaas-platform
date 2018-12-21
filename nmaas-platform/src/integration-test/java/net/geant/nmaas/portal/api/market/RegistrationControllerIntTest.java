package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.BaseControllerTestSetup;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RegistrationControllerIntTest extends BaseControllerTestSetup {

    @Before
    public void setup() {
        mvc = createMVC();
    }

    @Ignore
    @Test
    public void testSuccessfulRegistration() throws Exception {
        String randomUsername = "\"" + RandomStringUtils.random(10, true, false) + "\"";
        String randomPassword = "\"" + RandomStringUtils.random(10, true, false) + "\"";
        String randomFirstName = "\"" + RandomStringUtils.random(10, true, false) + "\"";
        String randomLastName = "\"" + RandomStringUtils.random(10, true, false) + "\"";

        String payload = String.format("{\"username\":%s,\"password\":%s,\"email\":\"geant.notification@gmail.com\",\"firstname\":%s,\"lastname\":%s,\"domainId\":1,\"termsOfUseAccepted\":true,\"privacyPolicyAccepted\":true}",
                randomUsername,
                randomPassword,
                randomFirstName,
                randomLastName);

    	mvc.perform(post("/api/auth/basic/registration")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .accept("*/*"))
                    .andExpect(status().isCreated());
    }
}