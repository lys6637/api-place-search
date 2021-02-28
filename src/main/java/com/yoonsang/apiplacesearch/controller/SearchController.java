package com.yoonsang.apiplacesearch.controller;

import com.yoonsang.apiplacesearch.dao.SearchVo;
import com.yoonsang.apiplacesearch.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping(value = "/place", produces = MediaType.APPLICATION_JSON_VALUE)
    public String searchPlaceByKeyword(SearchVo searchVo) throws Exception {

        if (!StringUtils.hasText(searchVo.getKeyword())) {
            throw new IllegalArgumentException("키워드는 필수 입니다.");
        }

        try {
            if (StringUtils.hasText(searchVo.getKeyword())) {
                Integer.parseInt(searchVo.getCurrentPage());
            }
            if (StringUtils.hasText(searchVo.getKeyword())) {
                Integer.parseInt(searchVo.getPageSize());
            }
        } catch (NumberFormatException ne) {
            throw new NumberFormatException("페이지는 숫자로 입력해주세요.");
        }

        return searchService.searchPlaceByKeyword(searchVo);
    }
}
