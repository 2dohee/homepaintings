package com.homepaintings.painting;

import com.homepaintings.annotation.AuthenticatedUser;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class PaintingController {

    private final PaintingFormValidator paintingFormValidator;
    private final PaintingService paintingService;

    @InitBinder("paintingForm")
    public void validatePaintingForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(paintingFormValidator);
    }

    @GetMapping("/admin/create-painting")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String createPaintingForm(@AuthenticatedUser Users user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("paintingForm", new PaintingForm());
        model.addAttribute("paintingType", Stream.of(PaintingType.values()).map(Enum::name).collect(Collectors.toList()));
        return "painting/admin/create-painting";
    }

    @PostMapping("/admin/create-painting")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String createPainting(@AuthenticatedUser Users user, Model model, @Valid PaintingForm paintingForm, Errors errors,
                                 RedirectAttributes attributes) {
        model.addAttribute("user", user);
        if (errors.hasErrors()) {
            model.addAttribute("errorMessage", "상품을 등록하지 못했습니다.");
            model.addAttribute("paintingType", Stream.of(PaintingType.values()).map(Enum::name).collect(Collectors.toList()));
            return "painting/admin/create-painting";
        }
        paintingService.createPainting(paintingForm);
        attributes.addFlashAttribute("successMessage", "상품을 등록했습니다.");
        return "redirect:/";
    }
}
