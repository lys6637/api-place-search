package com.yoonsang.apiplacesearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoonsang.apiplacesearch.domain.SearchTrend;
import com.yoonsang.apiplacesearch.domain.SearchTrendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendService {

    private final SearchTrendRepository searchTrendRepository;

    public String topTrendsByKeyword() throws Exception {

        Pageable pageable = PageRequest.of(0, 10, Sort.by("searchCount").descending());
        Page<SearchTrend> trendPage = searchTrendRepository.findAll(pageable);
        List<SearchTrend> trends = trendPage.getContent();

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(trends);

        return jsonString;
    }
}
