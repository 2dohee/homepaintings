package com.homepaintings.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UsersFactory {

    @Autowired UsersRepository usersRepository;

    public Users saveNewUser(String email, String nickname) {
        Users user = Users.builder()
                .email(email).nickname(nickname).password("12345678")
                .createdDateTime(LocalDateTime.now()).authority(Authority.ROLE_USER).build();
        user.generateEmailToken();
        Users savedUser = usersRepository.save(user);
        usersRepository.flush();
        return savedUser;
    }

}
