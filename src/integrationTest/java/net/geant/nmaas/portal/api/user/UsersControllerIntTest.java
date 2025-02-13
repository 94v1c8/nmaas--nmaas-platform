package net.geant.nmaas.portal.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.PasswordReset;
import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.api.domain.UserRoleView;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.utils.captcha.CaptchaValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_DOMAIN_ADMIN;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_GUEST;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_NOT_ACCEPTED;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_SYSTEM_ADMIN;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_TOOL_MANAGER;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_USER;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional(value=TxType.REQUIRES_NEW)
public class UsersControllerIntTest extends BaseControllerTestSetup {

    private static final String DOMAIN = "domtest";
    private static final String DOMAIN2 = "tetdom";

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UsersController userController;

    @Autowired
    private JWTTokenService jwtTokenService;

    @MockBean
    private CaptchaValidator captchaValidator;

    private String token;
    private String tokenForUserWithNotAcceptedTermsAndPolicy;

    private User userEntity;
    private User user3;

    private final Principal principal = mock(Principal.class);

    @BeforeEach
    void setUp() {
        mvc = createMVC();
        when(principal.getName()).thenReturn("admin");
        when(captchaValidator.verifyToken(anyString())).thenReturn(true);
        domains.createGlobalDomain();

        domains.createDomain(new DomainRequest(DOMAIN, DOMAIN, true));
        domains.createDomain(new DomainRequest(DOMAIN2, DOMAIN2, true));

        //Add extra users, default admin is already there
        User admin = new User("manager", true, "manager", domains.getGlobalDomain().get(), Collections.singletonList(ROLE_SYSTEM_ADMIN));
        admin.setEmail("manager@testemail.com");
        userRepo.save(admin);

        User userStub = new User("userEntity", true, "userEntity", domains.findDomain(DOMAIN).get(), Collections.singletonList(ROLE_USER));
        userStub.setFirstname("Test");
        userStub.setLastname("Test");
        userStub.setEmail("test@gmail.com");
        userEntity = userRepo.save(userStub);
        User user2 = new User("user2", true, "user2", domains.findDomain(DOMAIN).get(), Collections.singletonList(ROLE_USER));
        user2.setEmail("user2@testemail.com");
        userRepo.save(user2);

        user3 = new User("user3", true, "user3", domains.getGlobalDomain().get(), ROLE_NOT_ACCEPTED, false, false);
        user3.setEmail("user3@testemail.com");
        userRepo.save(user3);

        User domTestAdmin = new User("domAdmin", true, "domAdmin",domains.findDomain(DOMAIN2).get(), ROLE_DOMAIN_ADMIN, false, false);
        domTestAdmin.setEmail("domAdmin@testemail.com");
        userRepo.save(domTestAdmin);

        UserToken userToken = new UserToken(tokenService.getToken(admin), tokenService.getRefreshToken(admin));
        token = userToken.getToken();

        UserToken userNotAcceptedTermsAndPolicyToken = new UserToken(tokenService.getToken(user3), tokenService.getRefreshToken(user3));
        tokenForUserWithNotAcceptedTermsAndPolicy = userNotAcceptedTermsAndPolicyToken.getToken();

        prepareSecurity();
    }

    @Test
    void testDisableUser() throws Exception {
        mvc.perform(put("/api/users/status/" + userEntity.getId() + "?enabled=false")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();
        User result = userRepo.findById(userEntity.getId()).orElseThrow(IllegalArgumentException::new);
        assertFalse(result.isEnabled());
    }

    @Test
    void testEnableUser() throws Exception {
        mvc.perform(put("/api/users/status/" + userEntity.getId() + "?enabled=true")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
        User result = userRepo.findById(userEntity.getId()).orElseThrow(IllegalArgumentException::new);
        assertTrue(result.isEnabled());
    }

    @Test
    void testSetAcceptanceOfTermsOfUseAndPrivacyPolicy() throws Exception{
        mvc.perform(post("/api/users/terms/" + user3.getUsername())
                .header("Authorization", "Bearer " + tokenForUserWithNotAcceptedTermsAndPolicy)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
        User result = userRepo.findById(user3.getId()).orElseThrow(IllegalArgumentException::new);
        assertTrue(result.isTermsOfUseAccepted());
        assertTrue(result.isPrivacyPolicyAccepted());
    }

    @Test
    void testGetUsers() {
        assertEquals(6, userController.getUsers(Pageable.unpaged(), principal).size());
    }

    @Test
    void testGetRoles() {
        assertEquals(10, userController.getRoles().size());
    }

    @Test
    void testGetUser() throws MissingElementException {
        long id = userRepo.findByUsername("admin").get().getId();
        UserView user = (UserView)userController.retrieveUser(id, principal);
        assertEquals(Long.valueOf(id), user.getId());
        assertEquals("admin", user.getUsername());
    }

    @Test
    void shouldUpdateUserWithNewFirstNameAndLastName() {
        String newFirstName = "TestFirstName";
        String newLastName = "TestLastName";
        UserRequest userRequest = new UserRequest(null, userEntity.getUsername(), userEntity.getPassword());
        userRequest.setFirstname(newFirstName);
        userRequest.setLastname(newLastName);
        userController.updateUser(userEntity.getId(), userRequest, principal);
        User modUser1 = userRepo.findById(userEntity.getId()).get();

        assertEquals(modUser1.getFirstname(), newFirstName);
        assertEquals(modUser1.getLastname(), newLastName);
    }

    @Test
    void shouldUpdateUserWithNewEmail() {
        String newEmail = "admin@testemail.com";
        UserRequest userRequest = new UserRequest(null, userEntity.getUsername(), userEntity.getPassword());
        userRequest.setEmail(newEmail);
        userController.updateUser(userEntity.getId(), userRequest, principal);
        User modUser1 = userRepo.findById(userEntity.getId()).get();

        assertEquals(modUser1.getEmail(), newEmail);
    }

    @Test
    void shouldNotUpdateUserWithTakenEmail() {
        assertThrows(ProcessingException.class, () -> {
            String newEmail = user3.getEmail();
            UserRequest userRequest = new UserRequest(null, userEntity.getUsername(), userEntity.getPassword());
            userRequest.setEmail(newEmail);
            userController.updateUser(userEntity.getId(), userRequest, principal);
        });
    }

    @Test
    void shouldUpdateUserOwnData() {
        when(principal.getName()).thenReturn(user3.getUsername());
        assertDoesNotThrow(() -> {
            String newEmail = "admin@test.com";
            UserRequest userRequest = new UserRequest(null, user3.getUsername(), user3.getPassword());
            userRequest.setEmail(newEmail);
            userController.updateUser(user3.getId(), userRequest, principal);
        });
    }

    @Test
    void shouldNotUpdateOtherUserDataWithoutAdminRole() {
        when(principal.getName()).thenReturn(user3.getUsername());
        assertThrows(ProcessingException.class, () -> {
            String newEmail = "stub@nottakenmail.com";
            UserRequest userRequest = new UserRequest(null, userEntity.getUsername(), userEntity.getPassword());
            userRequest.setEmail(newEmail);
            userController.updateUser(userEntity.getId(), userRequest, principal);
        });
    }

    @Test
    void shouldNotUpdateUserWithoutDomainAdminRoleInUserDomain() {
        when(principal.getName()).thenReturn("domAdmin");
        assertThrows(ProcessingException.class, () -> {
            String newEmail = "stub@nottakenmail.com";
            UserRequest userRequest = new UserRequest(null, userEntity.getUsername(), userEntity.getPassword());
            userRequest.setEmail(newEmail);
            userController.updateUser(userEntity.getId(), userRequest, principal);
        });
    }

    @Test
    void testDeleteUser() {
        assertThrows(ProcessingException.class, () ->
            userController.deleteUser(userEntity.getId())
        );
    }

    @Test
    void testGetRolesAsString() {
        UserRole userRole1 = new UserRole(new User("TEST1"), new Domain("TEST", "TEST"), ROLE_USER);
        UserRole userRole2 = new UserRole(new User("TEST2"), new Domain("TEST", "TEST"), ROLE_SYSTEM_ADMIN);
        UserRole userRole3 = new UserRole(new User("TEST3"), new Domain("TEST", "TEST"), ROLE_DOMAIN_ADMIN);

        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(userRole1);
        userRoles.add(userRole2);
        userRoles.add(userRole3);

        assertEquals("ROLE_USER, ROLE_SYSTEM_ADMIN, ROLE_DOMAIN_ADMIN", userController.getRoleAsString(userRoles));
    }

    @Test
    void testGetMessageWhenUserUpdated(){
        UserRole userRole1 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), ROLE_USER);
        UserRole userRole2 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), ROLE_TOOL_MANAGER);

        List<UserRole> userRoles1 = new ArrayList<>();
        userRoles1.add(userRole1);
        userRoles1.add(userRole2);

        UserRoleView userRole3 = new UserRoleView();
        userRole3.setRole(ROLE_DOMAIN_ADMIN);
        userRole3.setDomainId(1L);

        Set<UserRoleView> userRoles3 = new HashSet<>();
        userRoles3.add(userRole3);

        User user = new User("user1");
        user.setFirstname("FirstName");
        user.setLastname("Lastname");
        user.setEmail("email@email.com");
        user.setRoles(userRoles1);
        user.setEnabled(true);

        UserRequest userRequest = new UserRequest(2L, "user2", "password");
        userRequest.setEmail("email1@email.com");
        userRequest.setFirstname("FirstName1");
        userRequest.setLastname("LastName1");
        userRequest.setRoles(userRoles3);

        String message = userController.getMessageWhenUserUpdated(user, userRequest);
        assertEquals(
                " Username [user1] -> [user2] Email [email@email.com] -> [email1@email.com] First name [FirstName] -> [FirstName1] Last name [Lastname] -> [LastName1] Enabled flag [true] -> [false] Roles changed [ROLE_USER, ROLE_TOOL_MANAGER] -> [ROLE_DOMAIN_ADMIN@domain1]",
                message
        );
    }

    @Test
    void testGetMessageWhenUserUpdatedWithSameRolesInDifferentOrder() {
        UserRole userRole1 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), ROLE_USER);
        UserRole userRole2 = new UserRole(new User("user1"), new Domain("TEST", "TEST"), ROLE_TOOL_MANAGER);

        User user = new User("user1");
        user.setFirstname("FirstName");
        user.setLastname("Lastname");
        user.setEmail("email@email.com");
        user.setRoles(Stream.of(userRole1, userRole2).collect(Collectors.toList()));
        user.setEnabled(true);

        UserRoleView userRole3 = new UserRoleView(ROLE_TOOL_MANAGER, 1L, "");
        UserRoleView userRole4 = new UserRoleView(ROLE_USER, 1L, "");

        UserRequest userRequest = new UserRequest(2L, "user2", "password");
        userRequest.setFirstname("FirstName1");
        userRequest.setLastname("LastName1");
        userRequest.setEmail("email1@email.com");
        userRequest.setRoles(Stream.of(userRole3, userRole4).collect(Collectors.toSet()));

        String message = userController.getMessageWhenUserUpdated(user, userRequest);
        assertEquals(
                " Username [user1] -> [user2] Email [email@email.com] -> [email1@email.com] First name [FirstName] -> [FirstName1] Last name [Lastname] -> [LastName1] Enabled flag [true] -> [false]",
                message
        );
    }

    @Test
    void testGetRoleWithDomainIdAsString() {
        UserRoleView userRole1 = new UserRoleView();
        userRole1.setRole(ROLE_USER);
        userRole1.setDomainId(1L);

        UserRoleView userRole2 = new UserRoleView();
        userRole2.setRole(ROLE_GUEST);
        userRole2.setDomainId(2L);

        Set<UserRoleView> userRoles = new LinkedHashSet<>();
        userRoles.add(userRole1);
        userRoles.add(userRole2);

        assertEquals("ROLE_USER@domain1, ROLE_GUEST@domain2", userController.getRoleWithDomainIdAsString(userRoles));
    }

    @Test
    void shouldValidateResetRequest() throws Exception {
        MvcResult result = mvc.perform(post("/api/users/reset/validate")
                .content(jwtTokenService.getResetToken(user3.getEmail()))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(user3.getEmail()));
    }

    @Test
    void shouldNotValidateResetRequest() {
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/users/reset/validate")
                    .content(jwtTokenService.getResetToken("notexisting@email.co.uk"))
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotAcceptable());
        });
    }

    @Test
    void shouldResetPassword() {
        PasswordReset passwordReset = new PasswordReset(jwtTokenService.getResetToken(user3.getEmail()), "test");
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/users/reset?token=test-token")
                    .content(new ObjectMapper().writeValueAsString(passwordReset))
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isAccepted());
        });
    }

    @Test
    void shouldNotResetPassword() {
        PasswordReset passwordReset = new PasswordReset(jwtTokenService.getResetToken("notexistingemail@mail.com"), "test");
        assertDoesNotThrow(() -> {
            mvc.perform(post("/api/users/reset?token=test-token")
                    .content(new ObjectMapper().writeValueAsString(passwordReset))
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotAcceptable());
        });
    }

    @AfterEach
    void tearUp() {
        userRepo.findAll().stream()
                .filter(user -> !user.getUsername().equalsIgnoreCase(UsersHelper.ADMIN.getUsername()))
                .forEach(user -> userRepo.delete(user));
        domains.getDomains().stream()
                .filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                .forEach(domain -> domains.removeDomain(domain.getId()));
    }
}
