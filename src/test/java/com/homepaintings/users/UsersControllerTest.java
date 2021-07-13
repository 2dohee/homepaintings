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
    @DisplayName("회원가입 페이지가 성공적으로 뜨는지 확인")
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/sign-up"));
    }

    @Test
    @DisplayName("회원가입 처리 확인 - 정상")
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
        assertNotEquals(byEmail.get().getPassword(), "12345678"); // 패스워드 인코딩 확인
        assertNotNull(byEmail.get().getEmailToken()); // 이메일 토큰 생성 확인
        then(emailService).should(times(1)).sendEmail(any(EmailMessage.class)); // 이메일 보내는지 확인
    }

    @Test
    @DisplayName("회원가입 처리 확인 - 잘못된 입력값1(다른 패스워드)")
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
        then(emailService).shouldHaveNoMoreInteractions(); // 이메일이 날아가지 않음
    }

    @Test
    @DisplayName("회원가입 처리 확인 - 잘못된 입력값1(중복 이메일)")
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
        then(emailService).shouldHaveNoMoreInteractions(); // 이메일이 날아가지 않음
    }

    @Test
    @DisplayName("회원가입 처리 확인 - 잘못된 입력값3(중복 닉네임)")
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
        then(emailService).shouldHaveNoMoreInteractions(); // 이메일이 날아가지 않음
    }

    @Test
    @DisplayName("이메일 인증 처리 - 정상")
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

        assertTrue(user.isEmailVerified()); // 인증된 상태로 바뀌는지 확인
    }

    @Test
    @DisplayName("이메일 인증 처리 - 잘못된 입력값1(없는 이메일)")
    void validateEmailToken_with_wrong_value_1() throws Exception {
        mockMvc.perform(get("/validate-email-token")
                    .param("email", "test@email.com")
                    .param("token", "token123"))
                    .andExpect(status().isOk())
                .andExpect(view().name("users/validated-email-token"))
                .andExpect(model().attribute("error", "해당 이메일이 없습니다."));
    }

    @Test
    @DisplayName("이메일 인증 처리 - 잘못된 입력값2(유효하지 않은 토큰)")
    void validateEmailToken_with_wrong_value_2() throws Exception {
        Users user = usersFactory.saveNewUser("test@email.com", "test", false);
        mockMvc.perform(get("/validate-email-token")
                    .param("email", user.getEmail())
                    .param("token", "token123"))
                    .andExpect(status().isOk())
                .andExpect(view().name("users/validated-email-token"))
                .andExpect(model().attribute("error", "유효한 이메일 토큰이 아닙니다."));
    }

    @Test
    @WithUser(value = TEST_EMAIL, authority = Authority.ROLE_ADMIN)
    @DisplayName("관리자 유저 관리 페이지가 성공적으로 뜨는지 확인")
    void admin_userInfo() throws Exception {
        for (int i = 0; i < 1000; i++) {
            boolean emailVerified = false;
            if (i % 2 == 0) emailVerified = true; // 짝수만 인증
            usersFactory.saveNewUser("user" + i + "@email.com", "닉네임" + i, emailVerified);
        }

        Map<String, Object> model = mockMvc.perform(get("/admin/users-info")
                    .param("page", "21") // 22 페이지
                    .param("onlyEmailVerified", "true")
                    .param("keywords", "네임") // 닉네임
                    .param("sorting", "생성일"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/admin/users-info"))
                .andExpect(model().attributeExists("user", "userList", "onlyEmailVerified", "keywords", "sorting"))
                .andExpect(model().attribute("currentFirstPage", 20))
                .andExpect(model().attribute("currentLastPage", 24))
                .andExpect(model().attribute("currentOffset", 420)) // 21 * 20
                .andExpect(authenticated().withAuthorities(List.of(new SimpleGrantedAuthority(Authority.ROLE_ADMIN.toString()))))
                .andReturn().getModelAndView().getModel();

        Page<Users> userList = (Page<Users>) model.get("userList");
        assertEquals(userList.getNumber(), 21);
        assertEquals(userList.getTotalPages(), 25); // 0 ~ 24
        assertEquals(userList.getTotalElements(), 500); // 25 * 20
        assertEquals(userList.getNumberOfElements(), 20);
        for (int i = 0; i < userList.getNumberOfElements(); i++) {
            assertEquals(userList.getContent().get(i).getEmail(), "user" + (158-2*i) + "@email.com"); // 인증된 생성일 내림차순 확인
        }
    }
}