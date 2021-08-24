package com.homepaintings.order.event;

import com.homepaintings.order.Orders;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderCanceledEvent {

    private final Orders order;

}
