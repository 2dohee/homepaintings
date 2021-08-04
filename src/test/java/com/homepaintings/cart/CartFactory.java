package com.homepaintings.cart;

import com.homepaintings.painting.Painting;
import com.homepaintings.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CartFactory {

    @Autowired CartRepository cartRepository;

    public Cart saveNewCart(Users user, Painting painting, int quantity, long totalPrice) {
        Cart cart = Cart.builder()
                .user(user).painting(painting).quantity(quantity).totalPrice(totalPrice).createdDateTime(LocalDateTime.now())
                .build();
        Cart savedCart = cartRepository.save(cart);
        cartRepository.flush();
        return savedCart;
    }

}
