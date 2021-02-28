package com.yoonsang.apiplacesearch.service;

import com.yoonsang.apiplacesearch.domain.SearchTrend;
import com.yoonsang.apiplacesearch.domain.SearchTrendRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class SynchronizeTest {

    @Autowired
    SearchService searchService;

    @Autowired
    SearchTrendRepository searchTrendRepository;

    @Test
    @DisplayName("DB Update Synchronize Test")
    void increaseCountForMultiThreadTest() throws InterruptedException {
        int numberOfExcute = 50;
        ExecutorService service = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(numberOfExcute);

        for (int i = 0; i < numberOfExcute; i++) {
            service.execute(() -> {
                searchService.increaseSearchCnt("kakao");
                latch.countDown();
            });
        }
        latch.await();
        SearchTrend searchTrend = searchTrendRepository.findWithKeywordForUpdate("kakao");
        assertThat(searchTrend.getSearchCount()).isEqualTo(60);
    }
}