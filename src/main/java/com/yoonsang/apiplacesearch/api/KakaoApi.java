package com.yoonsang.apiplacesearch.api;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.yoonsang.apiplacesearch.dao.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class KakaoApi {
    @Value("${kakao.key}")
    private String restApiKey;

    @Value("${kakao.maxSize}")
    private int maxSize;

    @Value("${kakao.maxPage}")
    private int maxPage;

    @Autowired
    RestTemplate restTemplate;

    private static final String API_SERVER_HOST = "https://dapi.kakao.com";
    private static final String SEARCH_PLACE_KEYWORD_PATH = "/v2/local/search/keyword.json";
    private static final String SEARCH_IMAGE_PATH = "/v2/search/image";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @HystrixCommand(fallbackMethod = "searchAPIFallback",
            commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000"),
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "3"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "5000")})
    public LinkedHashMap getKakaoSearchByKeyword(SearchVo searchVo) throws Exception {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

        int currentPage = Integer.parseInt(searchVo.getCurrentPage());
        int pageSize = Integer.parseInt(searchVo.getPageSize());

        if(currentPage < 1) currentPage=1;
        else if(currentPage > maxPage) currentPage=maxPage;
        if(pageSize < 1) pageSize=1;
        else if(pageSize > maxSize) pageSize=maxSize;

        String queryString = "?query=" + URLEncoder.encode(searchVo.getKeyword(), "UTF-8") + "&page=" + currentPage + "&size=" + pageSize;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + restApiKey);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        URI url = URI.create(API_SERVER_HOST + SEARCH_PLACE_KEYWORD_PATH + queryString);
        RequestEntity<Map> rq = new RequestEntity<>(headers, HttpMethod.GET, url);
        ResponseEntity<Map> re = restTemplate.exchange(rq, Map.class);

        //body 부분만 추출
        result = (LinkedHashMap) re.getBody();

        result.put("currentPage", currentPage);
        result.put("pageSize", pageSize);

        return result;
    }

    @HystrixCommand(fallbackMethod = "imageAPIFallback",
            commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000"),
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "3"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "5000")})
    public ArrayList<String> getKakoImageByKeyword(String title) throws Exception {
        ArrayList<String> result = new ArrayList<String>();

        String queryString = null;
        queryString = "?query=" + URLEncoder.encode(title, "UTF-8") + "&page=1&size=3";
        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", "KakaoAK " + restApiKey);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        URI url = URI.create(API_SERVER_HOST + SEARCH_IMAGE_PATH + queryString);
        RequestEntity<Map> rq = new RequestEntity<>(headers, HttpMethod.GET, url);
        ResponseEntity<Map> re = restTemplate.exchange(rq, Map.class);
        LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>) re.getBody();
        ArrayList<Map> documents = (ArrayList<Map>) body.get("documents");

        for (Map obj : documents) {
            String imgUrl = (String) obj.get("image_url");
            result.add(imgUrl);
        }

        return result;
    }

    @SuppressWarnings("unused")
    public LinkedHashMap searchAPIFallback(SearchVo searchVo, Throwable t) {
        log.error("KAKAO search API excetpion occurred : " + t.toString());
        return null;
    }

    @SuppressWarnings("unused")
    public ArrayList<String> imageAPIFallback(String title, Throwable t) {
        log.error("KAKAO image API exception occurred : " + t.toString());
        return null;
    }
}