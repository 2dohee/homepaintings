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
import java.util.Optional;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ModelMapper modelMapper;
    private final UsersService usersService;
    private final ProfileFormValidator profileFormValidator;

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

}
