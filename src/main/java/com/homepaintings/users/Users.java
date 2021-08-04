package com.homepaintings.users;

import com.homepaintings.order.Order;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Users {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String emailToken;

    private LocalDateTime emailTokenGeneratedDateTime;

    private boolean emailVerified;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Lob // VARCHAR(255) -> TEXT
    private String profileImage;

    private String phoneNumber;

    private String address1;

    private String address2;

    private String address3;

    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    private Authority authority; // 권한은 1개만 가질 수 있음

    @OneToMany(mappedBy = "user")
    private List<Order> orderList;

    public void generateEmailToken() {
        this.emailToken = UUID.randomUUID().toString() ;
        this.emailTokenGeneratedDateTime = LocalDateTime.now();
    }

    public boolean isValidEmailToken(String token) {
        return this.emailToken.equals(token) && this.emailTokenGeneratedDateTime.plusMinutes(10).isAfter(LocalDateTime.now());
    }
}
