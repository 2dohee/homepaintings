package com.homepaintings.order;

import com.homepaintings.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface OrderRepository extends JpaRepository<Orders, Long>, OrderRepositoryExtension {

    List<Orders> findByUserOrderByCreatedDateTimeDesc(Users user);

    List<Orders> findByIdInOrderByCreatedDateTimeDesc(List<Long> ids);

    Optional<Orders> findByIdAndUser(Long id, Users user);

}
