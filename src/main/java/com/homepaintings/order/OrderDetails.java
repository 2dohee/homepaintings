package com.homepaintings.order;

import com.homepaintings.painting.Painting;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class OrderDetails {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Painting painting;

    private int quantity;

}
