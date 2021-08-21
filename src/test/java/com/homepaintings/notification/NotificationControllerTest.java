package com.homepaintings.notification;

import com.homepaintings.order.DeliveryStatus;
import com.homepaintings.painting.Painting;
import com.homepaintings.painting.PaintingFactory;
import com.homepaintings.painting.PaintingType;
import com.homepaintings.review.Review;
import com.homepaintings.review.ReviewFactory;
import com.homepaintings.review.ReviewRepository;
import com.homepaintings.users.Users;
import com.homepaintings.users.UsersRepository;
import com.homepaintings.users.WithUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UsersRepository usersRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired NotificationFactory notificationFactory;

    final String TEST_EMAIL = "test@email.com";

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("알림 조회 페이지가 성공적으로 뜨는지 확인")
    void viewNotifications() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        notificationFactory.saveNewNotifications(user, 100);
        Map<String, Object> model = mockMvc.perform(get("/notifications")
                    .param("page", "2")) // 3 페이지
                .andExpect(status().isOk())
                .andExpect(view().name("notification/view-notification-list"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("isNew", true))
                .andExpect(model().attribute("currentFirstPage", 0))
                .andExpect(model().attribute("currentLastPage", 2))
                .andExpect(model().attribute("currentOffset", 40))
                .andReturn().getModelAndView().getModel();

        Page<Notification> notificationList = (Page<Notification>) model.get("notificationList");
        assertEquals(notificationList.getNumber(), 2);
        assertEquals(notificationList.getTotalPages(), 3); // 0 ~ 2
        assertEquals(notificationList.getTotalElements(), 50);
        assertEquals(notificationList.getNumberOfElements(), 10);
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("이미 읽은 알림 조회 페이지가 성공적으로 뜨는지 확인")
    void viewOldNotifications() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        notificationFactory.saveNewNotifications(user, 100);
        Map<String, Object> model = mockMvc.perform(get("/notifications/old")
                    .param("page", "2")) // 3 페이지
                .andExpect(status().isOk())
                .andExpect(view().name("notification/view-notification-list"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("isNew", false))
                .andExpect(model().attribute("currentFirstPage", 0))
                .andExpect(model().attribute("currentLastPage", 2))
                .andExpect(model().attribute("currentOffset", 40))
                .andReturn().getModelAndView().getModel();

        Page<Notification> notificationList = (Page<Notification>) model.get("notificationList");
        assertEquals(notificationList.getNumber(), 2);
        assertEquals(notificationList.getTotalPages(), 3); // 0 ~ 2
        assertEquals(notificationList.getTotalElements(), 50);
        assertEquals(notificationList.getNumberOfElements(), 10);
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("읽지 않은 알림을 체크하여 읽음으로 처리")
    void checkNotifications() throws Exception {
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        Notification notification = notificationFactory.saveNewNotifications(user, 1).get(0);
        notification.setChecked(false);
        List<String> checkedIdList = List.of(notification.getId().toString());
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.addAll("checkedIdList", checkedIdList);

        assertEquals(notification.isChecked(), false);
        mockMvc.perform(post("/notifications/check")
                    .params(map)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
        assertEquals(notification.isChecked(), true);
    }

}