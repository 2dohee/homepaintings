package com.homepaintings.notification;

import com.homepaintings.users.Users;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@NamedEntityGraph(name = "Notification.withUser", attributeNodes = {
        @NamedAttributeNode("user")
})

@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Users user;

    private String title;

    private String content;

    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private boolean checked;

}
