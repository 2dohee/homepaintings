package com.homepaintings.order;

import com.homepaintings.annotation.AuthenticatedUser;
import com.homepaintings.cart.Cart;
import com.homepaintings.cart.CartRepository;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;
    private final OrderService orderService;

    @GetMapping("/create-order")
    public String createOrderForm(@RequestParam ArrayList<Long> cartIdList, @AuthenticatedUser Users user, Model model) {
        List<Cart> cartList = cartRepository.findByIdInAndUserOrderByCreatedDateTimeDesc(cartIdList, user);
        OrderForm orderForm = modelMapper.map(user, OrderForm.class);
        orderForm.setCartIdList(cartIdList);
        model.addAttribute("user", user);
        model.addAttribute("cartList", cartList);
        model.addAttribute("orderForm", orderForm);
        return "order/create-order";
    }

    @PostMapping("/create-order")
    public String createOrder(@Valid OrderForm orderForm, Errors errors, @AuthenticatedUser Users user, Model model) {
        List<Cart> cartList = cartRepository.findByIdInAndUserOrderByCreatedDateTimeDesc(orderForm.getCartIdList(), user);
        if (errors.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("cartList", cartList);
            return "order/create-order";
        }
        if (!cartList.isEmpty()) {
            orderService.create(orderForm, cartList, user);
            cartRepository.deleteAllByIdInAndUserQuery(orderForm.getCartIdList(), user);
        }
        return "redirect:/";
    }
}
