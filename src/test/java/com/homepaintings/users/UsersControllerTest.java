package com.homepaintings.users;

import com.homepaintings.mail.EmailMessage;
import com.homepaintings.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class UsersControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UsersRepository usersRepository;
    @Autowired UsersService usersService;
    @Autowired UsersFactory usersFactory;
    @MockBean EmailService emailService;

    final String TEST_EMAIL = "test@email.com";

    @Test
    @DisplayName("???????????? ???????????? ??????????????? ????????? ??????")
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/sign-up"));
    }

    @Test
    @DisplayName("???????????? ?????? ?????? - ??????")
    void signUp() throws Exception {
        mockMvc.perform(post("/sign-up")
                    .param("email", "test@email.com")
                    .param("nickname", "test")
                    .param("password", "12345678")
                    .param("passwordAgain", "12345678")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("test@email.com"));

        Optional<Users> byEmail = usersRepository.findByEmail("test@email.com");
        assertFalse(byEmail.isEmpty());
        assertNotEquals(byEmail.get().getPassword(), "12345678"); // ???????????? ????????? ??????
        assertNotNull(byEmail.get().getEmailToken()); // ????????? ?????? ?????? ??????
        then(emailService).should(times(1)).sendEmail(any(EmailMessage.class)); // ????????? ???????????? ??????
    }

    @Test
    @DisplayName("???????????? ?????? ?????? - ????????? ?????????1(?????? ????????????)")
    void signUp_with_wrong_value_1() throws Exception {
        mockMvc.perform(post("/sign-up")
                    .param("email", "test@email.com")
                    .param("nickname", "test")
                    .param("password", "12345678!")
                    .param("passwordAgain", "12345678")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("users/sign-up"))
                .andExpect(model().hasErrors())
                .andExpect(unauthenticated());

        assertTrue(usersRepository.findByEmail("test@email.com").isEmpty());
        then(emailService).shouldHaveNoMoreInteractions(); // ???????????? ???????????? ??????
    }

    @Test
    @DisplayName("???????????? ?????? ?????? - ????????? ?????????1(?????? ?????????)")
    void signUp_with_wrong_value_2() throws Exception {
        usersFactory.saveNewUser("test@email.com", "test2", false);
        mockMvc.perform(post("/sign-up")
                    .param("email", "test@email.com")
                    .param("nickname", "test")
                    .param("password", "12345678")
                    .param("passwordAgain", "12345678")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("users/sign-up"))
                .andExpect(model().hasErrors())
                .andExpect(unauthenticated());

        assertTrue(usersRepository.findByNickname("test").isEmpty());
        then(emailService).shouldHaveNoMoreInteractions(); // ???????????? ???????????? ??????
    }

    @Test
    @DisplayName("???????????? ?????? ?????? - ????????? ?????????3(?????? ?????????)")
    void signUp_with_wrong_value_3() throws Exception {
        usersFactory.saveNewUser("test2@email.com", "test", false);
        mockMvc.perform(post("/sign-up")
                    .param("email", "test@email.com")
                    .param("nickname", "test")
                    .param("password", "12345678")
                    .param("passwordAgain", "12345678")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("users/sign-up"))
                .andExpect(model().hasErrors())
                .andExpect(unauthenticated());

        assertTrue(usersRepository.findByEmail("test@email.com").isEmpty());
        then(emailService).shouldHaveNoMoreInteractions(); // ???????????? ???????????? ??????
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ??????")
    void validateEmailToken() throws Exception {
        Users user = usersFactory.saveNewUser("test@email.com", "test", false);
        mockMvc.perform(get("/validate-email-token")
                    .param("email", user.getEmail())
                    .param("token", user.getEmailToken()))
                .andExpect(status().isOk())
                .andExpect(view().name("users/validated-email-token"))
                .andExpect(model().attribute("email", user.getEmail()))
                .andExpect(model().attribute("nickname", user.getNickname()))
                .andExpect(authenticated().withUsername(user.getEmail()));

        assertTrue(user.isEmailVerified()); // ????????? ????????? ???????????? ??????
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ?????????1(?????? ?????????)")
    void validateEmailToken_with_wrong_value_1() throws Exception {
        mockMvc.perform(get("/validate-email-token")
                    .param("email", "test@email.com")
                    .param("token", "token123"))
                    .andExpect(status().isOk())
                .andExpect(view().name("users/validated-email-token"))
                .andExpect(model().attribute("error", "?????? ???????????? ????????????."));
    }

    @Test
    @DisplayName("????????? ?????? ?????? - ????????? ?????????2(???????????? ?????? ??????)")
    void validateEmailToken_with_wrong_value_2() throws Exception {
        Users user = usersFactory.saveNewUser("test@email.com", "test", false);
        mockMvc.perform(get("/validate-email-token")
                    .param("email", user.getEmail())
                    .param("token", "token123"))
                    .andExpect(status().isOk())
                .andExpect(view().name("users/validated-email-token"))
                .andExpect(model().attribute("error", "????????? ????????? ????????? ????????????."));
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("????????? ?????? ?????? ???????????? ??????????????? ????????? ??????")
    void admin_userInfo() throws Exception {
        for (int i = 1; i <= 210; i++) {
            usersFactory.saveNewUser("user" + i + "@email.com", "?????????" + i, true);
        }

        Map<String, Object> model = mockMvc.perform(get("/admin/users-info")
                    .param("page", "10") // 11 ?????????
                    .param("onlyEmailVerified", "true")
                    .param("keywords", "??????") // ?????????
                    .param("sorting", "?????????"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/admin/users-info"))
                .andExpect(model().attributeExists("user", "userList", "onlyEmailVerified", "keywords", "sorting"))
                .andExpect(model().attribute("currentFirstPage", 10))
                .andExpect(model().attribute("currentLastPage", 10))
                .andExpect(model().attribute("currentOffset", 200)) // 10 * 20
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))))
                .andReturn().getModelAndView().getModel();

        Page<Users> userList = (Page<Users>) model.get("userList");
        assertEquals(userList.getNumber(), 10);
        assertEquals(userList.getTotalPages(), 11); // 0 ~ 10
        assertEquals(userList.getTotalElements(), 210);
        assertEquals(userList.getNumberOfElements(), 10);
    }
}