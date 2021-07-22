package com.homepaintings.painting;

import com.homepaintings.annotation.AuthenticatedUser;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class PaintingController {

    private final PaintingFormValidator paintingFormValidator;
    private final PaintingService paintingService;
    private final PaintingRepository paintingRepository;

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
        return "redirect:/view-paintings";
    }

    @GetMapping("/view-paintings")
    public String viewPaintings(@AuthenticatedUser Users user, Model model,
                                @PageableDefault(size = 12) Pageable pageable, PaintingSearchForm paintingSearchForm) {
        Page<Painting> paintingsList = paintingRepository.findPaintings(pageable, paintingSearchForm);
        int currentFirstPage = paintingsList.getNumber() / 10 * 10;
        int currentLastPage = Math.min(currentFirstPage + 9, paintingsList.getTotalPages() - 1);
        model.addAttribute("user", user);
        model.addAttribute("paintingType", Stream.of(PaintingType.values()).map(Enum::name).collect(Collectors.toList()));
        model.addAttribute("paintingList", paintingsList);
        model.addAttribute("paintingSearchForm", paintingSearchForm);
        model.addAttribute("currentFirstPage", currentFirstPage);
        model.addAttribute("currentLastPage", currentLastPage);
        model.addAttribute("currentOffset", paintingsList.getNumber() * paintingsList.getSize());
        return "painting/view-paintings";
    }
}
