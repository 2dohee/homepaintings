package com.homepaintings.cart;

import com.homepaintings.annotation.AuthenticatedUser;
import com.homepaintings.painting.Painting;
import com.homepaintings.painting.PaintingRepository;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
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
    public ResponseEntity addCart(@RequestBody @Valid CartForm cartForm, Errors errors,
                                  @AuthenticatedUser Users user) {
        Optional<Painting> byId = paintingRepository.findByName(cartForm.getPaintingName());
        if (byId.isEmpty() || errors.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        cartService.addToCart(user, byId.get(), cartForm.getQuantity());
        return ResponseEntity.ok().body("카트에 물건을 담았습니다.");
    }

    @GetMapping("/list")
    public String viewCartList(@AuthenticatedUser Users user, Model model) {
        List<Cart> cartList = cartRepository.findByUserOrderByCreatedDateTimeDesc(user);
        model.addAttribute("user", user);
        model.addAttribute("cartList", cartList);
        return "cart/view-cart-list";
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity removeCart(@RequestBody CartListForm cartListForm, @AuthenticatedUser Users user) {
        ArrayList<Long> cartIdList = cartListForm.getCartIdList();
        cartService.remove(cartIdList, user);
        return ResponseEntity.ok().body("카트에 물품을 삭제했습니다.");
    }

}
