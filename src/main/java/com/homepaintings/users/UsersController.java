package com.homepaintings.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UsersController {

    private final SignUpFormValidator signUpFormValidator;
    private final UsersService usersService;
    private final UsersRepository usersRepository;

    @InitBinder("signUpForm")
    public void validateSignUpForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        return "users/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "users/sign-up";
        }
        usersService.signUp(signUpForm);
        return "redirect:/";
    }

    @GetMapping("/validate-email-token")
    public String validateEmailToken(String email, String token, Model model) {
        Optional<Users> byEmail = usersRepository.findByEmail(email);
        String view = "users/validated-email-token";
        if (byEmail.isEmpty()) {
            model.addAttribute("error", "해당 이메일이 없습니다.");
            return view;
        }
        if (!byEmail.get().isValidEmailToken(token)) {
            model.addAttribute("error", "유효한 이메일 토큰이 아닙니다.");
            return view;
        }
        usersService.completeSignUp(byEmail.get());
        model.addAttribute("email", byEmail.get().getEmail());
        model.addAttribute("nickname", byEmail.get().getNickname());
        return view;
    }
}
