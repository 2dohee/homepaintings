package com.homepaintings.users;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsersController {

    @GetMapping("/sign-up")
    public String signUp() {
        return "users/sign-up";
    }
}
