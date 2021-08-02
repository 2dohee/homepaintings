package com.homepaintings.cart;

import com.homepaintings.painting.Painting;
import com.homepaintings.users.Users;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@NamedEntityGraph(name = "Cart.withPainting", attributeNodes = {
        @NamedAttributeNode("painting")
})

@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Cart {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Users user;

    @ManyToOne
    private Painting painting;

    private int quantity;

    private long totalPrice;

    private LocalDateTime createdDateTime;

}
