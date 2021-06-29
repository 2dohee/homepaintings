package com.homepaintings.users;

import com.homepaintings.annotation.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ModelMapper modelMapper;
    private final UsersService usersService;
    private final ProfileFormValidator profileFormValidator;
    private final PasswordFormValidator passwordFormValidator;

    @InitBinder("passwordForm")
    public void validatePasswordForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    @GetMapping("/update-profile")
    public String updateProfileForm(@AuthenticatedUser Users user, Model model) {
        model.addAttribute(modelMapper.map(user, ProfileForm.class));
        model.addAttribute("user", user);
        return "users/settings/update-profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@AuthenticatedUser Users user, @Valid ProfileForm profileForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        profileFormValidator.validate(profileForm, errors, user);
        if (errors.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "프로필 수정에 실패했습니다.");
            return "users/settings/update-profile";
        }
        usersService.updateProfile(user, profileForm);
        attributes.addFlashAttribute("successMessage", "프로필을 수정했습니다.");
        return "redirect:/settings/update-profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(@AuthenticatedUser Users user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute(modelMapper.map(user, PasswordForm.class));
        return "users/settings/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticatedUser Users user, @Valid PasswordForm passwordForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "비밀번호 변경에 실패했습니다.");
            return "users/settings/change-password";
        }
        usersService.changePassword(user, passwordForm.getPassword());
        attributes.addFlashAttribute("successMessage", "비밀번호를 변경했습니다.");
        return "redirect:/settings/change-password";
    }

    @GetMapping("/verify-email")
    public String verifyEmailForm(@AuthenticatedUser Users user, Model model) {
        model.addAttribute("user", user);
        return "users/settings/verify-email";
    }

    @PostMapping("/verify-email")
    public String verifyEmail(@AuthenticatedUser Users user, Model model, RedirectAttributes attributes) {
        if (user.isValidEmailToken(user.getEmailToken())) {
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "아직 이메일이 유효하므로 보내진 이메일로 인증하실 수 있습니다.");
            return "users/settings/verify-email";
        }
        usersService.resendEmailToken(user);
        attributes.addFlashAttribute("successMessage", "인증코드를 재발송했습니다.");
        return "redirect:/settings/verify-email";
    }

    @GetMapping("sign-out")
    public String signOutForm(@AuthenticatedUser Users user, Model model) {
        model.addAttribute("user", user);
        return "users/settings/sign-out";
    }

    @PostMapping("sign-out")
    public String signOut(@AuthenticatedUser Users user) {
        usersService.deleteUser(user);
        return "redirect:/";
    }

}
