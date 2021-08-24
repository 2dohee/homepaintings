package com.homepaintings.order.event;

import com.homepaintings.order.Orders;

public class DeliveryChangedToCompletedEvent extends DeliveryChangedEvent {
    public DeliveryChangedToCompletedEvent(Orders order) {
        super(order, "주문이 배송완료되었습니다.(주문번호: " + order.getId() + ")");
    }
}
