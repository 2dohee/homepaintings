package com.homepaintings.notification;

import com.homepaintings.users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph("Notification.withUser")
    Page<Notification> findByUserAndChecked(Users user, boolean checked, Pageable pageable);

    @EntityGraph("Notification.withUser")
    List<Notification> findByIdInAndUser(List<Long> checkedIdList, Users user);

    long countByUserAndChecked(Users user, boolean checked);

}
