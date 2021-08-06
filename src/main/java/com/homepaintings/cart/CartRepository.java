package com.homepaintings.cart;

import com.homepaintings.users.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph("Cart.withPainting")
    List<Cart> findByUserOrderByCreatedDateTimeDesc(Users user);

    @EntityGraph("Cart.withPainting")
    List<Cart> findByIdInAndUserOrderByCreatedDateTimeDesc(List<Long> ids, Users user);

    @Transactional
    @Modifying
    @Query("delete from Cart c where c.id in :ids and c.user = :user")
    void deleteAllByIdInAndUserQuery(@Param("ids") List<Long> ids, @Param("user") Users user);

}
