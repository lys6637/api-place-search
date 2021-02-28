package com.yoonsang.apiplacesearch.controller;

import com.yoonsang.apiplacesearch.service.SearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SearchController.class)
public class SearchControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SearchService searchService;

    @Test
    @DisplayName("Controller 작동 테스트")
    public void ControllerRequestTest() throws  Exception {
        RequestBuilder reqBuilder = MockMvcRequestBuilders.get("/place?keyword=kakao").contentType(MediaType.APPLICATION_JSON);
        mvc.perform(reqBuilder).andExpect(status().isOk()).andDo(print());
    }

    @Test
    @DisplayName("Keyword 파라미터 검증 테스트")
    public void KeywordExceptionTest() {
        RequestBuilder reqBuilder = MockMvcRequestBuilders.get("/place").contentType(MediaType.APPLICATION_JSON);
        Throwable result = catchThrowable(() ->  mvc.perform(reqBuilder));
        assertTrue(result.getMessage().contains("키워드는 필수 입니다."));
    }

    @Test
    @DisplayName("Page 파라미터 검증 테스트")
    public void pageParamExceptionTest() {
        RequestBuilder reqBuilder = MockMvcRequestBuilders.get("/place?keyword=kakao&pageSize=one").contentType(MediaType.APPLICATION_JSON);
        Throwable result = catchThrowable(() ->  mvc.perform(reqBuilder));
        assertTrue(result.getMessage().contains("페이지는 숫자로 입력해주세요."));
    }
}
