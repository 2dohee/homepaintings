package com.homepaintings.main;

import com.homepaintings.annotation.AuthenticatedUser;
import com.homepaintings.users.Users;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@AuthenticatedUser Users user, Model model) {
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "index";
    }

}
