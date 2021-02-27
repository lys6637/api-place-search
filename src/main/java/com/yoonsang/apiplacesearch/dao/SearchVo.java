package com.yoonsang.apiplacesearch.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SearchVo {

    String keyword;
    String currentPage = "1";
    String pageSize = "10";
}
