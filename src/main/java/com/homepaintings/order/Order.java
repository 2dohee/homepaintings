package com.homepaintings.order;

import com.homepaintings.cart.Cart;
import com.homepaintings.users.Users;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "orders")
@Getter @Setter @EqualsAndHashCode(of ="id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Order {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Users user;

    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address1;

    @Column(nullable = false)
    private String address2;

    @Column(nullable = false)
    private String address3;

    private Long totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetails> orderDetailsList = new ArrayList<>();

    private LocalDateTime createdDateTime;

    public void addAllDetails(List<OrderDetails> orderDetailsList) {
        this.orderDetailsList.addAll(orderDetailsList);
        orderDetailsList.forEach(orderDetails -> orderDetails.setOrder(this));
    }
}
