package com.homepaintings.cart;

import com.homepaintings.painting.Painting;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public void addToCart(Users user, Painting painting, int quantity) {
        Cart newCart = Cart.builder()
                .user(user).painting(painting).quantity(quantity).createdDateTime(LocalDateTime.now())
                .build();
        cartRepository.save(newCart);
    }
}
