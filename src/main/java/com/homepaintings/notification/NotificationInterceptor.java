package com.homepaintings.notification;

import com.homepaintings.users.UserPrincipal;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationRepository notificationRepository;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (modelAndView != null && !(modelAndView.getViewName().startsWith("redirect:") || modelAndView.getView() instanceof RedirectView)
            && authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            Users user = ((UserPrincipal) authentication.getPrincipal()).getUser();
            long count = notificationRepository.countByUserAndChecked(user, false);
            modelAndView.addObject("notificationCount", count);
        }
    }
}
