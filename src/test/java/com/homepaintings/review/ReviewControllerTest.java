package com.homepaintings.review;

import com.homepaintings.cart.Cart;
import com.homepaintings.order.DeliveryStatus;
import com.homepaintings.order.Orders;
import com.homepaintings.painting.*;
import com.homepaintings.users.Authority;
import com.homepaintings.users.Users;
import com.homepaintings.users.UsersRepository;
import com.homepaintings.users.WithUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired PaintingFactory paintingFactory;
    @Autowired ReviewFactory reviewFactory;
    @Autowired UsersRepository usersRepository;
    @Autowired ReviewRepository reviewRepository;

    final String TEST_EMAIL = "test@email.com";

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("리뷰 페이지가 성공적으로 뜨는지 확인")
    void createPaintingForm() throws Exception {
        Painting painting = paintingFactory.saveNewPainting("painting", PaintingType.FIGURE, 10000, 100);
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        reviewFactory.saveNewReviews(user, painting, 100);
        Map<String, Object> model = mockMvc.perform(get("/painting/" + painting.getId() + "/review/list")
                .param("page", "3")) // 4 페이지
                .andExpect(status().isOk())
                .andExpect(view().name("review/view-review-list"))
                .andExpect(model().attributeExists("user", "paintingId"))
                .andExpect(model().attribute("currentFirstPage", 0))
                .andExpect(model().attribute("currentLastPage", 4))
                .andExpect(model().attribute("currentOffset", 60))
                .andReturn().getModelAndView().getModel();

        Page<Review> reviewList = (Page<Review>) model.get("reviewList");
        assertEquals(reviewList.getNumber(), 3);
        assertEquals(reviewList.getTotalPages(), 5); // 0 ~ 4
        assertEquals(reviewList.getTotalElements(), 100);
        assertEquals(reviewList.getNumberOfElements(), 20);
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("리뷰 작성 페이지가 성공적으로 뜨는지 확인")
    void writeReviewForm() throws Exception {
        Painting painting = paintingFactory.saveNewPainting("painting", PaintingType.FIGURE, 10000, 100);
        mockMvc.perform(get("/painting/" + painting.getId() + "/review/write"))
                .andExpect(status().isOk())
                .andExpect(view().name("review/write-review"))
                .andExpect(model().attributeExists("user", "reviewForm"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("리뷰 작성")
    void writeReview() throws Exception {
        Painting painting = paintingFactory.saveNewPainting("painting", PaintingType.FIGURE, 10000, 100);
        mockMvc.perform(post("/painting/" + painting.getId() + "/review/write")
                    .param("rank", "5")
                    .param("image", "")
                    .param("content", "테스트내용")
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/painting/" + painting.getId() + "/review/list"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        Review review = reviewRepository.findByPaintingOrderByCreatedDateTimeDesc(painting, PageRequest.of(0, 1)).getContent().get(0);
        assertEquals(review.getContent(), "테스트내용");
        assertEquals(review.getRank(), 5);
    }

    @Test
    @WithUser(TEST_EMAIL)
    @DisplayName("리뷰 삭제")
    void removeReview() throws Exception {
        Painting painting = paintingFactory.saveNewPainting("painting", PaintingType.FIGURE, 10000, 100);
        Users user = usersRepository.findByEmail(TEST_EMAIL).get();
        Review review = reviewFactory.saveNewReviews(user, painting, 1).get(0);

        mockMvc.perform(post("/painting/" + painting.getId() + "/review/remove/" + review.getId()).with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/painting/" + painting.getId() + "/review/list"))
                .andExpect(authenticated().withUsername(TEST_EMAIL));

        assertEquals(reviewRepository.findAll().size(), 0);
    }

}