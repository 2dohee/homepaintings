package com.homepaintings.review;

import com.homepaintings.painting.Painting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph("Review.withUser")
    Page<Review> findByPaintingOrderByCreatedDateTimeDesc(Painting painting, Pageable pageable);

}
