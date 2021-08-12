package com.homepaintings.order;

import com.homepaintings.annotation.AuthenticatedUser;
import com.homepaintings.cart.Cart;
import com.homepaintings.cart.CartRepository;
import com.homepaintings.painting.Painting;
import com.homepaintings.users.Users;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
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
        return "redirect:/order/list";
    }

    @GetMapping("/order/list")
    public String viewOrderList(@RequestParam(defaultValue = "ALL") String orderStatus, @AuthenticatedUser Users user, Model model) {
        List<Orders> orderList = orderRepository.findByUserOrderByCreatedDateTimeDesc(user);
        classifyOrderList(orderList, orderStatus, model);
        model.addAttribute("user", user);
        return "order/view-order-list";
    }

    @GetMapping("/admin/order/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String viewUsersOrderList(@PageableDefault(size = 2, sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                                     @RequestParam(defaultValue = "READY") String deliveryStatus,
                                     @RequestParam(defaultValue = "") String keywords,
                                     @AuthenticatedUser Users user, Model model) {
        model.addAttribute("user", user);
        Page<Orders> orderList = orderRepository.findUsersOrder(pageable, deliveryStatus, keywords);

        int currentFirstPage = orderList.getNumber() / 10 * 10;
        int currentLastPage = Math.min(currentFirstPage + 9, orderList.getTotalPages() - 1);
        model.addAttribute("orderList", orderList);
        model.addAttribute("currentFirstPage", currentFirstPage);
        model.addAttribute("currentLastPage", currentLastPage);
        model.addAttribute("currentOffset", orderList.getNumber() * orderList.getSize());
        model.addAttribute("keywords", keywords);
        model.addAttribute("deliveryStatus", deliveryStatus);
        return "order/admin/users-order-list";
    }

    private void classifyOrderList(List<Orders> orderList, String orderStatus, Model model) {
        ArrayList<Orders> completedList = new ArrayList<>();
        ArrayList<Orders> uncompletedList = new ArrayList<>();

        orderList.forEach(o -> {
            if (o.getDeliveryStatus().equals(DeliveryStatus.COMPLETED)) completedList.add(o);
            else uncompletedList.add(o);
        });
        model.addAttribute("allOrderCount", orderList.size());
        model.addAttribute("completedCount", completedList.size());
        model.addAttribute("uncompletedCount", uncompletedList.size());
        model.addAttribute("orderStatus", orderStatus);

        if (orderStatus.equals("COMPLETED")) model.addAttribute("orderList", completedList);
        else if (orderStatus.equals("UNCOMPLETED")) model.addAttribute("orderList", uncompletedList);
        else model.addAttribute("orderList", orderList);
    }

    @GetMapping("/order/{id}/details")
    public ResponseEntity getOrderDetails(@PathVariable Long id) {
        List<OrderDetails> byOrderId = orderDetailsRepository.findByOrderId(id);
        List<OrderDetailsForm> orderDetailsFormList = byOrderId.stream().map(o -> {
            Painting p = o.getPainting();
            return new OrderDetailsForm(p.getName(), p.getType(), p.getPrice(), o.getQuantity());
        }).collect(Collectors.toList());
        return ResponseEntity.ok().body(orderDetailsFormList);
    }

    @PostMapping("/admin/order/list/change/delivery-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String changeDeliveryStatusToOrderList(DeliveryStatusForm deliveryStatusForm, RedirectAttributes attributes) {
        ArrayList<Long> orderIdList = deliveryStatusForm.getOrderIdList();
        ArrayList<DeliveryStatus> deliveryStatusList = deliveryStatusForm.getDeliveryStatusList();
        if (orderIdList.size() == deliveryStatusList.size()) {
            orderService.changeDeliveryStatus(orderIdList, deliveryStatusList);
            attributes.addFlashAttribute("successMessage", "배송 상태를 변경했습니다.");
        }
        return "redirect:/admin/order/list";
    }
}
