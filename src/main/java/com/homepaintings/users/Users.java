package com.homepaintings.users;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
public class Users {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String emailToken;

    private LocalDateTime emailTokenGeneratedDateTime;

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

}
