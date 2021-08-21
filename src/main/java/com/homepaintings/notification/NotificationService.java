package com.homepaintings.notification;

import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void check(List<Long> checkedIdList, Users user) {
        List<Notification> notificationList = notificationRepository.findByIdInAndUser(checkedIdList, user);
        notificationList.forEach(notification -> notification.setChecked(true));
    }
}
