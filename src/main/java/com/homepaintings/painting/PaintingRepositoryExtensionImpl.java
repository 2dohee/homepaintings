package com.homepaintings.painting;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class PaintingRepositoryExtensionImpl extends QuerydslRepositorySupport implements PaintingRepositoryExtension {

    QPainting painting = QPainting.painting;

    public PaintingRepositoryExtensionImpl() {
        super(Painting.class);
    }

    @Override
    public Page<Painting> findPaintings(Pageable pageable, PaintingSearchForm paintingSearchForm) {
        String keywords = paintingSearchForm.getKeywords();
        String type  = paintingSearchForm.getType();
        String sorting = paintingSearchForm.getSorting();
        boolean asc = paintingSearchForm.isAsc();

        BooleanBuilder builder = new BooleanBuilder();
        String[] keywordArray = keywords.split(" ");
        for (String keyword : keywordArray) {
            builder.and(painting.name.containsIgnoreCase(keyword)
                    .or(painting.description.containsIgnoreCase(keyword)));
        }
        builder.and(eqType(type));

        JPQLQuery<Painting> query = from(painting)
                .where(builder)
                .orderBy(sorting(sorting, asc));
        JPQLQuery<Painting> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Painting> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    private BooleanExpression eqType(String type) {
        for (PaintingType p : PaintingType.values()) {
            if (type.equals(p.toString())) return painting.type.eq(p);
        }
        return null; // 기본값은 전체
    }

    private OrderSpecifier sorting(String sorting, boolean asc) {
        Order order = asc ? Order.ASC : Order.DESC;
        if (sorting.equals("이름")) return new OrderSpecifier(order, painting.name);
        if (sorting.equals("가격")) return new OrderSpecifier(order, painting.price);
        if (sorting.equals("재고")) return new OrderSpecifier(order, painting.stock);
        return new OrderSpecifier(order, painting.createdDateTime); // 기본값은 생성일 기준
    }
}
