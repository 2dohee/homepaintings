package com.homepaintings.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;

    public Users signUp(SignUpForm signUpForm) {
        // 회원가입 처리
        return null;
    }
}
