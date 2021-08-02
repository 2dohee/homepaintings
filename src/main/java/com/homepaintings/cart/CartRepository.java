package com.homepaintings.cart;

import com.homepaintings.users.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph("Cart.withPainting")
    List<Cart> findByUserOrderByCreatedDateTimeDesc(Users user);

}
