package com.homepaintings.users;

import com.homepaintings.annotation.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

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
    public String signUpForm(@AuthenticatedUser Users user, Model model) {
        if (user != null) {
            model.addAttribute("user", user);
            return "redirect:/";
        }
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
    public String validateEmailToken(String email, String token, @AuthenticatedUser Users user, Model model) {
        if (user != null) {
            model.addAttribute("user", user);
        }
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
        model.addAttribute("user", byEmail.get());
        return view;
    }

    @GetMapping("/login")
    public String login(@AuthenticatedUser Users user, Model model) {
        if (user != null) {
            model.addAttribute("user", user);
            return "redirect:/";
        }
        return "users/login";
    }

    @GetMapping("/admin/users-info")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String usersInfo(@AuthenticatedUser Users user, Model model,
                            @PageableDefault(size = 3) Pageable pageable,
                            @RequestParam(defaultValue = "false") Boolean onlyEmailVerified,
                            @RequestParam(defaultValue = "") String keywords,
                            @RequestParam(defaultValue = "생성일") String sorting) {
        model.addAttribute("user", user);
        Page<Users> userList = usersRepository.findUsersInfo(pageable, onlyEmailVerified, keywords, sorting);
        model.addAttribute("userList", userList);
        int currentFirstPage = userList.getNumber() / 10 * 10;
        int currentLastPage = Math.min(currentFirstPage + 9, userList.getTotalPages() - 1);
        model.addAttribute("currentFirstPage", currentFirstPage);
        model.addAttribute("currentLastPage", currentLastPage);
        model.addAttribute("currentOffset", userList.getNumber() * userList.getSize());
        model.addAttribute("onlyEmailVerified", onlyEmailVerified);
        model.addAttribute("keywords", keywords);
        model.addAttribute("sorting", sorting);
        return "users/admin/users-info";
    }
}
