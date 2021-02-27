package com.yoonsang.apiplacesearch.controller;

import com.yoonsang.apiplacesearch.service.TrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TrendController {

    private final TrendService trendService;

    @GetMapping(value = "/trend", produces = MediaType.APPLICATION_JSON_VALUE)
    public String topTrendsByKeyword() throws Exception {

        String result = trendService.topTrendsByKeyword();

        return result;
    }
}
