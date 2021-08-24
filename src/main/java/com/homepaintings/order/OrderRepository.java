package com.homepaintings.order;

import com.homepaintings.users.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface OrderRepository extends JpaRepository<Orders, Long>, OrderRepositoryExtension {

    List<Orders> findByUserOrderByCreatedDateTimeDesc(Users user);

    @EntityGraph("Orders.withUser")
    List<Orders> findOrdersWithUserByIdInOrderByCreatedDateTimeDesc(List<Long> ids);

    @EntityGraph("Orders.withUser")
    Optional<Orders> findOrdersWithUserByIdAndUser(Long id, Users user);

}
