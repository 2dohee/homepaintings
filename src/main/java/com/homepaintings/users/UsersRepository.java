package com.homepaintings.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UsersRepository extends JpaRepository<Users, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<Users> findByEmail(String email);
}
