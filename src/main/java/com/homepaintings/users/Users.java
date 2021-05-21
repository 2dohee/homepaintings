package com.homepaintings.users;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
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

    public void generateEmailToken() {
        this.emailToken = UUID.randomUUID().toString() ;
        this.emailTokenGeneratedDateTime = LocalDateTime.now();
    }

    public boolean isValidEmailToken(String token) {
        return this.emailToken.equals(token) && this.emailTokenGeneratedDateTime.plusSeconds(20).isAfter(LocalDateTime.now());
    }
}
