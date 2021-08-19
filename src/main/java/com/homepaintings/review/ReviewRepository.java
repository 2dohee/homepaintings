package com.homepaintings.review;

import com.homepaintings.painting.Painting;
import com.homepaintings.users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph("Review.withUser")
    Page<Review> findByPaintingOrderByCreatedDateTimeDesc(Painting painting, Pageable pageable);

    @Transactional
    @Modifying
    @Query("delete from Review r where r.id = :id and r.user = :user")
    void deleteByIdAndUserQuery(@Param("id") Long id, @Param("user") Users user);

}
