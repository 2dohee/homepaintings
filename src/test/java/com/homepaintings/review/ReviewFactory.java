package com.homepaintings.review;

import com.homepaintings.painting.Painting;
import com.homepaintings.painting.PaintingRepository;
import com.homepaintings.painting.PaintingType;
import com.homepaintings.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReviewFactory {

    @Autowired ReviewRepository reviewRepository;

    public List<Review> saveNewReviews(Users user, Painting painting, int num) {
        List<Review> reviewList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Review review = Review.builder()
                    .user(user).painting(painting).content("내용"+num).rank(num%5 + 1).image("")
                    .createdDateTime(LocalDateTime.now()).updatedDateTime(LocalDateTime.now())
                    .build();
            reviewList.add(review);
        }
        return reviewRepository.saveAll(reviewList);
    }

}
