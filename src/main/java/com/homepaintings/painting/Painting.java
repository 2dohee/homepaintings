package com.homepaintings.painting;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Painting {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private PaintingType type;

    private int price;

    private int stock;

    @Lob
    private String image;

    private String description;

    private LocalDateTime createdDateTime;

    private LocalDateTime updatedDateTime;

}
