package com.homepaintings.review;

import com.homepaintings.painting.Painting;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ModelMapper modelMapper;
    private final ReviewRepository reviewRepository;

    public void write(ReviewForm reviewForm, Users user, Painting painting) {
        Review review = modelMapper.map(reviewForm, Review.class);
        review.setUser(user);
        review.setPainting(painting);
        review.setCreatedDateTime(LocalDateTime.now());
        review.setUpdatedDateTime(LocalDateTime.now());
        reviewRepository.save(review);
    }

    public void remove(Long id, Users user) {
        reviewRepository.deleteByIdAndUserQuery(id, user);
    }
}
