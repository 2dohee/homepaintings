package com.homepaintings.order;

import com.homepaintings.cart.Cart;
import com.homepaintings.order.event.DeliveryChangedToCompletedEvent;
import com.homepaintings.order.event.DeliveryChangedToShippingEvent;
import com.homepaintings.order.event.OrderCanceledEvent;
import com.homepaintings.order.event.OrderCreatedEvent;
import com.homepaintings.users.Users;
import com.homepaintings.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final UsersRepository usersRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public void create(OrderForm orderForm, List<Cart> cartList, Users user) {
        Orders order = modelMapper.map(orderForm, Orders.class);
        order.setTotalPrice(cartList.stream().mapToLong(Cart::getTotalPrice).sum());
        order.setCreatedDateTime(LocalDateTime.now());
        order.setDeliveryStatus(DeliveryStatus.READY);
        List<OrderDetails> orderDetailsList = createOrderDetailsList(cartList);
        order.addAllDetails(orderDetailsList);
        usersRepository.findByEmail(user.getEmail()).get().addOrder(order); // lazy loading 을 위해 user 의 상태를 persistent 로 전환
        Orders savedOrder = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder));
    }

    private List<OrderDetails> createOrderDetailsList(List<Cart> cartList) {
        return cartList.stream().map(cart -> {
            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setPainting(cart.getPainting());
            orderDetails.setQuantity(cart.getQuantity());
            return orderDetails;
        }).collect(Collectors.toList());
    }

    public void changeDeliveryStatus(ArrayList<Long> orderIdList, ArrayList<DeliveryStatus> deliveryStatusList) {
        List<Orders> orderList = orderRepository.findOrdersWithUserByIdInOrderByCreatedDateTimeDesc(orderIdList);
        for (int i = 0; i < orderList.size(); i++) {
            Orders order = orderList.get(i);
            DeliveryStatus deliveryStatus = deliveryStatusList.get(i);
            if (!order.getDeliveryStatus().equals(deliveryStatus)) { // 배송상태가 변경되었다면 알림 발송
                if (deliveryStatus.equals(DeliveryStatus.SHIPPING)) eventPublisher.publishEvent(new DeliveryChangedToShippingEvent(order));
                else if (deliveryStatus.equals(DeliveryStatus.COMPLETED)) eventPublisher.publishEvent(new DeliveryChangedToCompletedEvent(order));
                order.setDeliveryStatus(deliveryStatus);
            }
        }
        orderRepository.saveAll(orderList);
    }

    public void removeOrder(Orders order) {
        eventPublisher.publishEvent(new OrderCanceledEvent(order));
        orderRepository.delete(order);
    }
}
