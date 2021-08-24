package com.homepaintings.order.event;

import com.homepaintings.notification.Notification;
import com.homepaintings.notification.NotificationRepository;
import com.homepaintings.notification.NotificationType;
import com.homepaintings.order.Orders;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Async
@Component
@Transactional
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) {
        Orders order = orderCreatedEvent.getOrder();
        createNotification("주문이 생성되었습니다.(주문번호: " + order.getId() + ")", order, NotificationType.ORDER_CREATED);
    }

    @EventListener
    public void handleOrderCanceledEvent(OrderCanceledEvent orderCanceledEvent) {
        Orders order = orderCanceledEvent.getOrder();
        createNotification("주문이 취소되었습니다.(주문번호: " + order.getId() + ")", order, NotificationType.ORDER_CANCELED);
    }

    @EventListener
    public void handleDeliveryChangedEvent(DeliveryChangedEvent deliveryChangedEvent) {
        createNotification(deliveryChangedEvent.getMessage(), deliveryChangedEvent.getOrder(), NotificationType.DELIVERY_CHANGED);
    }

    private void createNotification(String message, Orders order, NotificationType type) {
        Notification notification = Notification.builder()
                .message(message).type(type).user(order.getUser())
                .checked(false).createdDateTime(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

}
