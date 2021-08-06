package com.homepaintings.order;

import com.homepaintings.cart.Cart;
import com.homepaintings.users.Users;
import com.homepaintings.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final UsersRepository usersRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    public void create(OrderForm orderForm, List<Cart> cartList, Users user) {
        Order order = modelMapper.map(orderForm, Order.class);
        order.setTotalPrice(cartList.stream().mapToLong(Cart::getTotalPrice).sum());
        order.setCreatedDateTime(LocalDateTime.now());
        order.addAllDetails(createOrderDetailsList(cartList));
        usersRepository.findByEmail(user.getEmail()).get().addOrder(order); // lazy loading 을 위해 user 의 상태를 persistent 로 전환
        orderRepository.save(order);
        System.out.println();
    }

    private List<OrderDetails> createOrderDetailsList(List<Cart> cartList) {
        return cartList.stream().map(cart -> {
            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setPainting(cart.getPainting());
            orderDetails.setQuantity(cart.getQuantity());
            return orderDetails;
        }).collect(Collectors.toList());
    }

}
