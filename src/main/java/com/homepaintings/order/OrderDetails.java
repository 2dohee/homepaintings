package com.homepaintings.order;

import com.homepaintings.painting.Painting;
import lombok.*;

import javax.persistence.*;

@NamedEntityGraph(name = "OrderDetails.withPainting", attributeNodes = {
        @NamedAttributeNode("painting")
})

@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class OrderDetails {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Orders order;

    @ManyToOne
    private Painting painting;

    private Integer quantity;

}
