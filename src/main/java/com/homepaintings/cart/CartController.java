package com.homepaintings.cart;

import com.homepaintings.annotation.AuthenticatedUser;
import com.homepaintings.painting.Painting;
import com.homepaintings.painting.PaintingRepository;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final PaintingRepository paintingRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity addPaintingToCart(@RequestBody @Valid CartForm cartForm, Errors errors,
                                            @AuthenticatedUser Users user) {
        Optional<Painting> byId = paintingRepository.findByName(cartForm.getPaintingName());
        if (byId.isEmpty() || errors.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        cartService.addToCart(user, byId.get(), cartForm.getQuantity());
        return ResponseEntity.ok().body("카트에 물건을 담았습니다.");
    }

}
