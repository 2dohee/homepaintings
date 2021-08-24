package com.homepaintings.order.event;

import com.homepaintings.order.Orders;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeliveryChangedEvent {

    private final Orders order;

    private final String message;

}
