package com.homepaintings.notification;

import com.homepaintings.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationFactory {

    @Autowired NotificationRepository notificationRepository;

    public List<Notification> saveNewNotifications(Users user, int num) {
        List<Notification> notificationList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Notification notification = Notification.builder()
                    .title("알림"+i).content("내용"+i).user(user).checked(i % 2 == 0 ? true : false)
                    .type(i % 3 == 0 ? NotificationType.ORDER_CREATED : i % 3 == 1 ? NotificationType.ORDER_CANCELED : NotificationType.DELIVERY_CHANGED)
                    .createdDateTime(LocalDateTime.now()).build();
            notificationList.add(notification);
        }
        return notificationRepository.saveAll(notificationList);
    }
}
