package com.homepaintings.review;

import com.homepaintings.painting.Painting;
import com.homepaintings.users.Users;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Review {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Users user;

    @ManyToOne
    private Painting painting;

    @Column(nullable = false)
    private Integer rank;

    private String image;

    @Column(nullable = false)
    private String content;

    private LocalDateTime createdDateTime;

    private LocalDateTime updatedDateTime;

}
