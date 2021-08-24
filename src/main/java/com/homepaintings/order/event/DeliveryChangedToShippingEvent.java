package com.homepaintings.order.event;

import com.homepaintings.order.Orders;

public class DeliveryChangedToShippingEvent extends DeliveryChangedEvent {
    public DeliveryChangedToShippingEvent(Orders order) {
        super(order, "주문이 발송되었습니다.(주문번호: " + order.getId() + ")");
    }
}
