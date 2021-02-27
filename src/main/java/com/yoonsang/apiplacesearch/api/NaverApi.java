package com.yoonsang.apiplacesearch.api;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.yoonsang.apiplacesearch.dao.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class NaverApi {
    @Value("${naver.client-id}")
    private String clientID;

    @Value("${naver.client-secret}")
    private String clientSecrect;

    private static final String API_SERVER_HOST = "https://openapi.naver.com";
    private static final String SEARCH_PLACE_KEYWORD_PATH = "/v1/search/local.json";
    private static final String SEARCH_IMAGE_PATH = "/v1/search/image";

    @HystrixCommand(fallbackMethod = "searchAPIFallback", commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "3"),
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "5000")})
    public LinkedHashMap getSearchByKeyword(SearchVo searchVo) throws Exception {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000); //타임아웃 설정 5초
        factory.setReadTimeout(5000);//타임아웃 설정 5초
        RestTemplate restTemplate = new RestTemplate();

        String queryString = "?query=" + URLEncoder.encode(searchVo.getKeyword(), "UTF-8") + "&start=" + searchVo.getCurrentPage() + "&display=" + searchVo.getPageSize();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", clientID);
        headers.add("X-Naver-Client-Secret", clientSecrect);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        URI url = URI.create(API_SERVER_HOST + SEARCH_PLACE_KEYWORD_PATH + queryString);
        RequestEntity<Map> rq = new RequestEntity<>(headers, HttpMethod.GET, url);
        ResponseEntity<Map> re = restTemplate.exchange(rq, Map.class);
        //body 부분만 추출
        result = (LinkedHashMap) re.getBody();

        return result;
    }

    @HystrixCommand(fallbackMethod = "imageAPIFallback",
            commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000"),
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "3"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "5000")})
    public ArrayList<String> getImageByKeyword(String title) throws Exception {
        ArrayList<String> result = new ArrayList<String>();

        String queryString = "?query=" + URLEncoder.encode(title, "UTF-8") + "&display=3&start=1";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.add("X-Naver-Client-Id", clientID);
        headers.add("X-Naver-Client-Secret", clientSecrect);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        URI url = URI.create(API_SERVER_HOST + SEARCH_IMAGE_PATH + queryString);
        RequestEntity<Map> rq = new RequestEntity<>(headers, HttpMethod.GET, url);
        ResponseEntity<Map> re = restTemplate.exchange(rq, Map.class);
        LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>) re.getBody();
        ArrayList<Map> items = (ArrayList<Map>) body.get("items");

        for (Map obj : items) {
            String imgUrl = (String) obj.get("link");
            result.add(imgUrl);
        }

        return result;
    }

    @SuppressWarnings("unused")
    public LinkedHashMap searchAPIFallback(SearchVo searchVo, Throwable t) {
        log.error("NAVER search API excetpion occurred : " + t.toString());
        return null;
    }

    @SuppressWarnings("unused")
    public ArrayList<String> imageAPIFallback(String title, Throwable t) {
        log.error("NAVER search API excetpion occurred : " + t.toString());
        return null;
    }
}
