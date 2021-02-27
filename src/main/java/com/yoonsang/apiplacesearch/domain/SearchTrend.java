package com.yoonsang.apiplacesearch.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
public class SearchTrend {
    @Id
    private String keyword;

    private int searchCount;

    @Builder
    public SearchTrend(String keyword, int searchCount) {
        this.keyword = keyword;
        this.searchCount = searchCount;
    }

    public void increaseTrendCnt() {
        this.searchCount++;
    }
}