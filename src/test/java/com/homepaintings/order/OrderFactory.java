package com.homepaintings.order;

import com.homepaintings.painting.Painting;
import com.homepaintings.painting.PaintingFactory;
import com.homepaintings.painting.PaintingType;
import com.homepaintings.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderFactory {

    @Autowired OrderService orderService;
    @Autowired PaintingFactory paintingFactory;
    @Autowired OrderRepository orderRepository;

    public List<Orders> saveNewOrders(Users user, List<Painting> paintingList, int num) {
        List<Orders> ordersList = new ArrayList<>();
        long totalPrice = paintingList.get(0).getPrice() * 10 + paintingList.get(1).getPrice() * 10 + paintingList.get(2).getPrice() * 10;
        for (int i = 0; i < num; i++) {
            DeliveryStatus deliveryStatus = i % 3 == 0 ? DeliveryStatus.READY : (i % 3 == 1 ? DeliveryStatus.SHIPPING : DeliveryStatus.COMPLETED);
            
            Orders order = Orders.builder().receiver("수령인" + i).phoneNumber("010" + String.format("%08d", i))
                    .address1("123-456").address2("xx시 xx동").address3("xx로 x길 1-" + i)
                    .deliveryStatus(deliveryStatus).totalPrice(totalPrice).createdDateTime(LocalDateTime.now())
                    .orderDetailsList(new ArrayList<>()).build();
            OrderDetails orderDetails1 = OrderDetails.builder().painting(paintingList.get(0)).quantity(10).build();
            OrderDetails orderDetails2 = OrderDetails.builder().painting(paintingList.get(1)).quantity(10).build();
            OrderDetails orderDetails3 = OrderDetails.builder().painting(paintingList.get(2)).quantity(10).build();
            order.addAllDetails(List.of(orderDetails1, orderDetails2, orderDetails3));
            user.addOrder(order);
            ordersList.add(order);
        }
        return orderRepository.saveAll(ordersList);
    }

}
