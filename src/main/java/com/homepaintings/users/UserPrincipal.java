package com.homepaintings.users;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UserPrincipal extends User {

    private Users user;

    public UserPrincipal(Users user) {
        super(user.getEmail(), user.getPassword(), List.of(new SimpleGrantedAuthority(user.getAuthority().toString())));
        this.user = user;
    }
}
