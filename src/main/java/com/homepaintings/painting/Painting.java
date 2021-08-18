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

    private Integer price;

    private Integer stock;

    @Lob
    private String image;

    @Lob
    private String description;

    private LocalDateTime createdDateTime;

    private LocalDateTime updatedDateTime;

}
