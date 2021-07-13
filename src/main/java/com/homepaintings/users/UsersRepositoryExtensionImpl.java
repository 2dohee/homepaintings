package com.homepaintings.users;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class UsersRepositoryExtensionImpl extends QuerydslRepositorySupport implements UsersRepositoryExtension {

    QUsers user = QUsers.users;

    public UsersRepositoryExtensionImpl() {
        super(Users.class);
    }

    @Override
    public Page<Users> findUsersInfo(Pageable pageable, Boolean onlyEmailVerified, String keywords, String sorting) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(user.authority.eq(Authority.ROLE_USER))
                .and(eqOnlyEmailVerified(onlyEmailVerified));

        String[] keywordArray = keywords.split(" ");
        for (String keyword : keywordArray) {
            builder.and(user.email.containsIgnoreCase(keyword)
                    .or(user.nickname.containsIgnoreCase(keyword))
                    .or(user.phoneNumber.containsIgnoreCase(keyword)));
        }
        JPQLQuery<Users> query = from(user)
                .where(builder)
                .orderBy(sorting(sorting));
        JPQLQuery<Users> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Users> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    BooleanExpression eqOnlyEmailVerified(Boolean onlyEmailVerified) {
        if (onlyEmailVerified) return user.emailVerified.isTrue();
        return null;
    }

    private OrderSpecifier sorting(String sorting) {
        if (sorting.equals("이메일")) return user.email.asc();
        if (sorting.equals("닉네임")) return user.nickname.asc();
        return user.createdDateTime.desc(); // 기본값은 생성일 기준
    }
}
