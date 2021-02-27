package com.yoonsang.apiplacesearch.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;

public interface SearchTrendRepository extends JpaRepository<SearchTrend, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Transactional
    @Query("select t from SearchTrend t where t.keyword = :keyword")
    SearchTrend findWithKeywordForUpdate(@Param("keyword") String keyword);

}
