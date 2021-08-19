package com.homepaintings.review;

import com.homepaintings.annotation.AuthenticatedUser;
import com.homepaintings.painting.Painting;
import com.homepaintings.painting.PaintingRepository;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/painting/{id}/review")
@RequiredArgsConstructor
public class ReviewController {

    private final PaintingRepository paintingRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    @GetMapping("/list")
    public String viewReviewList(@PageableDefault(size = 20, sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                                 @PathVariable Long id, @AuthenticatedUser Users user, Model model) {
        model.addAttribute("user", user);
        Optional<Painting> byId = paintingRepository.findById(id);
        if (byId.isEmpty()) return "error";

        Page<Review> reviewList = reviewRepository.findByPaintingOrderByCreatedDateTimeDesc(byId.get(), pageable);
        int currentFirstPage = reviewList.getNumber() / 10 * 10;
        int currentLastPage = Math.min(currentFirstPage + 9, reviewList.getTotalPages() - 1);

        model.addAttribute("paintingId", id);
        model.addAttribute("currentFirstPage", currentFirstPage);
        model.addAttribute("currentLastPage", currentLastPage);
        model.addAttribute("currentOffset", reviewList.getNumber() * reviewList.getSize());
        model.addAttribute("reviewList", reviewList);
        return "review/view-review-list";
    }

    @GetMapping("/write")
    public String writeReviewForm(@PathVariable Long id, @AuthenticatedUser Users user, Model model) {
        model.addAttribute("user", user);
        Optional<Painting> byId = paintingRepository.findById(id);
        if (byId.isEmpty()) return "error";
        model.addAttribute("reviewForm", new ReviewForm());
        return "review/write-review";
    }

    @PostMapping("/write")
    public String writeReview(@PathVariable Long id, ReviewForm reviewForm, Errors errors, @AuthenticatedUser Users user, Model model) {
        Optional<Painting> byId = paintingRepository.findById(id);
        if (byId.isEmpty()) {
            model.addAttribute("user", user);
            return "error";
        }
        if (errors.hasErrors()) return "review/write-review";
        reviewService.write(reviewForm, user, byId.get());
        return "redirect:/painting/" + id + "/review/list";
    }

    @PostMapping("/remove/{reviewId}")
    public String removeReview(@PathVariable Long id, @PathVariable Long reviewId, @AuthenticatedUser Users user, Model model) {
        Optional<Painting> byId = paintingRepository.findById(id);
        if (byId.isEmpty()) {
            model.addAttribute("user", user);
            return "error";
        }
        reviewService.remove(reviewId, user);
        return "redirect:/painting/" + id + "/review/list";
    }

}
