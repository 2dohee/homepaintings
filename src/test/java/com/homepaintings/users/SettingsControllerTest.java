package com.homepaintings.users;

import com.homepaintings.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
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
}