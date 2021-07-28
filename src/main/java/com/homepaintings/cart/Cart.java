package com.homepaintings.cart;

import com.homepaintings.painting.Painting;
import com.homepaintings.users.Users;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

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

    private LocalDateTime createdDateTime;

}
