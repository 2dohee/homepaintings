package com.homepaintings.notification;

import com.homepaintings.annotation.AuthenticatedUser;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String viewNotifications(@PageableDefault(size = 20, sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                                    @AuthenticatedUser Users user, Model model) {
        Page<Notification> notificationList = notificationRepository.findByUserAndChecked(user, false, pageable);
        model.addAttribute("user", user);
        model.addAttribute("isNew", true);
        putNotificationModel(model, notificationList);
        return "notification/view-notification-list";
    }

    @GetMapping("/notifications/old")
    public String viewOldNotifications(@PageableDefault(size = 20, sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                                       @AuthenticatedUser Users user, Model model) {
        Page<Notification> notificationList = notificationRepository.findByUserAndChecked(user, true, pageable);
        model.addAttribute("user", user);
        model.addAttribute("isNew", false);
        putNotificationModel(model, notificationList);
        return "notification/view-notification-list";
    }

    private void putNotificationModel(Model model, Page<Notification> notificationList) {
        int currentFirstPage = notificationList.getNumber() / 10 * 10;
        int currentLastPage = Math.min(currentFirstPage + 9, notificationList.getTotalPages() - 1);
        model.addAttribute("notificationList", notificationList);
        model.addAttribute("currentFirstPage", currentFirstPage);
        model.addAttribute("currentLastPage", currentLastPage);
        model.addAttribute("currentOffset", notificationList.getNumber() * notificationList.getSize());
    }

    @PostMapping("/notifications/check")
    public String checkNotifications(@RequestParam List<Long> checkedIdList, @AuthenticatedUser Users user) {
        notificationService.check(checkedIdList, user);
        return "redirect:/notifications";
    }
}
