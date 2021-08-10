package com.homepaintings.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Long> {

    @EntityGraph("OrderDetails.withPainting")
    List<OrderDetails> findByOrderId(Long orderId);

}
