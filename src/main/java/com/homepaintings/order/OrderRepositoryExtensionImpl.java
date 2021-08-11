package com.homepaintings.order;

import com.homepaintings.painting.QPainting;
import com.homepaintings.users.QUsers;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class OrderRepositoryExtensionImpl extends QuerydslRepositorySupport implements OrderRepositoryExtension {

    QOrders order = QOrders.orders;
    QOrderDetails orderDetails = QOrderDetails.orderDetails;

    public OrderRepositoryExtensionImpl() {
        super(Orders.class);
    }

    @Override
    public Page<Orders> findUsersOrder(Pageable pageable, String deliveryStatus, String keywords) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(eqDeliveryStatus(deliveryStatus));

        String[] keywordArray = keywords.split(" ");
        for (String keyword : keywordArray) {
            builder.and(order.receiver.containsIgnoreCase(keyword)
                    .or(order.address1.containsIgnoreCase(keyword))
                    .or(order.address2.containsIgnoreCase(keyword))
                    .or(order.address3.containsIgnoreCase(keyword)));
        }
        JPQLQuery<Orders> query = from(order)
                .where(builder)
                .leftJoin(order.user, QUsers.users).fetchJoin()
                .leftJoin(order.orderDetailsList, QOrderDetails.orderDetails).fetchJoin()
                .leftJoin(orderDetails.painting, QPainting.painting).fetchJoin()
                .distinct();
        JPQLQuery<Orders> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Orders> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    private Predicate eqDeliveryStatus(String deliveryStatus) {
        if (deliveryStatus.equals(DeliveryStatus.COMPLETED.toString())) return order.deliveryStatus.eq(DeliveryStatus.COMPLETED);
        else if (deliveryStatus.equals(DeliveryStatus.SHIPPING.toString())) return order.deliveryStatus.eq(DeliveryStatus.SHIPPING);
        else return order.deliveryStatus.eq(DeliveryStatus.READY); // 기본값은 READY 상태
    }
}
