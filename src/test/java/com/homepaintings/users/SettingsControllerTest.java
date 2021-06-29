package com.homepaintings.users;

import com.homepaintings.mail.EmailMessage;
import com.homepaintings.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UsersRepository usersRepository;
    @Autowired UsersService usersService;
    @Autowired UsersFactory usersFactory;
    @MockBean EmailService emailService;

    final String TEST_EMAIL = "test@email.com";

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("프로필 수정 화면이 제대로 뜨는지 확인")
    void updateProfileForm() throws Exception {
        mockMvc.perform(get("/settings/update-profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/settings/update-profile"))
                .andExpect(model().attributeExists("user", "profileForm"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("프로필 수정 - 정상")
    void signUp() throws Exception {
        mockMvc.perform(post("/settings/update-profile")
                    .param("nickname", "test2")
                    .param("phoneNumber", "")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/update-profile"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        assertEquals(user.getNickname(), "test2");
        assertEquals(user.getPhoneNumber(), "");
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("프로필 수정 - 잘못된 입력값1(중복 닉네임)")
    void signUp_with_wrong_value_1() throws Exception {
        usersFactory.saveNewUser("test2@email.com", "test2");
        mockMvc.perform(post("/settings/update-profile")
                    .param("nickname", "test2")
                    .param("phoneNumber", "")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("users/settings/update-profile"))
                .andExpect(model().attributeExists("user", "errorMessage"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertNotEquals(usersRepository.findByEmail(TEST_EMAIL).get().getNickname(), "test2");
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("프로필 수정 - 잘못된 입력값2(잘못된 형식의 번호)")
    void signUp_with_wrong_value_2() throws Exception {
        mockMvc.perform(post("/settings/update-profile")
                    .param("nickname", "test")
                    .param("phoneNumber", "11112345678")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("users/settings/update-profile"))
                .andExpect(model().attributeExists("user", "errorMessage"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertNotEquals(usersRepository.findByEmail(TEST_EMAIL).get().getPhoneNumber(), "11112345678");
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("비밀번호 변경 화면이 제대로 뜨는지 확인")
    void changePasswordForm() throws Exception {
        mockMvc.perform(get("/settings/change-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/settings/change-password"))
                .andExpect(model().attributeExists("user", "passwordForm"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("비밀번호 변경 - 정상")
    void changePassword() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        String prevPassword = user.getPassword();

        mockMvc.perform(post("/settings/change-password")
                    .param("password", "55555555")
                    .param("passwordAgain", "55555555")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/change-password"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertNotEquals(prevPassword, user.getPassword());
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("비밀번호 변경 - 잘못된 입력값(불일치)")
    void signUp_with_wrong_value() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        String prevPassword = user.getPassword();

        mockMvc.perform(post("/settings/change-password")
                    .param("password", "55555555")
                    .param("passwordAgain", "66666666")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("users/settings/change-password"))
                .andExpect(model().attributeExists("user", "errorMessage"))
                .andExpect(model().hasErrors())
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertEquals(prevPassword, user.getPassword());
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("이메일 인증 화면이 제대로 뜨는지 확인")
    void verifyEmailForm() throws Exception {
        mockMvc.perform(get("/settings/verify-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/settings/verify-email"))
                .andExpect(model().attributeExists("user"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("이메일 인증코드 재전송 - 성공")
    void verifyEmail() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        String emailToken = user.getEmailToken();
        user.setEmailTokenGeneratedDateTime(LocalDateTime.now().minusMinutes(11)); // 재전송을 하기 위해 시간을 돌림
        usersRepository.save(user);
        usersRepository.flush();

        mockMvc.perform(post("/settings/verify-email")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/verify-email"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertNotEquals(emailToken, user.getEmailToken());
        then(emailService).should(times(2)).sendEmail(any(EmailMessage.class));
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("이메일 인증코드 재전송 - 실패(쿨타임)")
    void verifyEmail_cooltime() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        String emailToken = user.getEmailToken();

        mockMvc.perform(post("/settings/verify-email")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("users/settings/verify-email"))
                .andExpect(model().attributeExists("user", "errorMessage"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertEquals(emailToken, user.getEmailToken()); // 안 바뀜
        then(emailService).should(times(1)).sendEmail(any(EmailMessage.class)); // 처음 가입 시만 1번 보냄
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("계정 삭제 화면이 제대로 뜨는지 확인")
    void signOutForm() throws Exception {
        mockMvc.perform(get("/settings/sign-out"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/settings/sign-out"))
                .andExpect(model().attributeExists("user"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("계정 삭제")
    void signOut() throws Exception {
        assertFalse(usersRepository.findByEmail(TEST_EMAIL).isEmpty());
        mockMvc.perform(post("/settings/sign-out")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());

        assertTrue(usersRepository.findByEmail(TEST_EMAIL).isEmpty());
    }
}