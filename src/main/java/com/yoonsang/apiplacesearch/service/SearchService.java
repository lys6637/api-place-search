package com.yoonsang.apiplacesearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoonsang.apiplacesearch.api.KakaoApi;
import com.yoonsang.apiplacesearch.api.NaverApi;
import com.yoonsang.apiplacesearch.dao.SearchVo;
import com.yoonsang.apiplacesearch.domain.SearchTrend;
import com.yoonsang.apiplacesearch.domain.SearchTrendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    private final SearchTrendRepository searchTrendRepository;
    private final KakaoApi kakaoApiHelper;
    private final NaverApi naverApiHelper;
    private static final String TEST_CIRCUIT_BREAKER = "testCircuitBreaker";

    @Transactional
    public String searchPlaceByKeyword(SearchVo searchVo) throws Exception {

        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        ArrayList<Map> resultPlaces = new ArrayList<Map>();
        LinkedHashMap<String, Object> searchBody = new LinkedHashMap<String, Object>();
        ArrayList<Map> searchedPlaces = new ArrayList<>();
        LinkedHashMap<String, Object> newMeta = new LinkedHashMap<String, Object>();

        boolean isKakaoSearchAPI = true;
        String keyword = searchVo.getKeyword();

        // Keyword로 검색하기 & meta 재조립
        searchBody = kakaoApiHelper.getSearchByKeyword(searchVo); //Kakao API 호출

        if (searchBody == null) {
            isKakaoSearchAPI = false;
            searchBody = naverApiHelper.getSearchByKeyword(searchVo); // Kakao 장애시 Naver API 호출
            searchedPlaces = (ArrayList<Map>) searchBody.get("items");
            newMeta.put("totalPages", 1); // Naver 검색 API는 page를 1개만 제공하기 때문에 1로 고정
            newMeta.put("totalElements", searchBody.get("total"));
            newMeta.put("pageSize", searchBody.get("display"));
            newMeta.put("currentPage", searchBody.get("start"));
        } else {
            searchedPlaces = (ArrayList<Map>) searchBody.get("documents");
            LinkedHashMap<String, Object> meta = (LinkedHashMap<String, Object>) searchBody.get("meta");
            newMeta.put("totalPages", meta.get("pageable_count"));
            newMeta.put("totalElements", meta.get("total_count"));
            newMeta.put("pageSize", searchVo.getPageSize());
            newMeta.put("currentPage", searchVo.getCurrentPage());
        }
        newMeta.put("keyword", keyword);

        // ImageURL 검색하기
        String imgSearchKey = "";
        if (isKakaoSearchAPI) imgSearchKey = "place_name";
        else imgSearchKey = "title";

        boolean isKakaoImgAPI = true;

        for (Map obj : searchedPlaces) {
            String title = (String) obj.get(imgSearchKey);
            ArrayList<String> imageUrls = kakaoApiHelper.getImageByKeyword(title);
            if (imageUrls == null) {
                isKakaoImgAPI = false;
                break;
            }
            Map<String, Object> place = new HashMap<String, Object>();
            place.put("imageUrls", imageUrls);
            place.put("title", title);
            resultPlaces.add(place);
        }

        if (!isKakaoImgAPI) {
            for (Map obj : searchedPlaces) {
                String title = (String) obj.get(imgSearchKey);
                title = title.replaceAll("<b>", "");
                title = title.replaceAll("</b>", "");
                ArrayList<String> imageUrls = naverApiHelper.getImageByKeyword(title); //Kakao Img API 장애시 Naver API 호출
                if (imageUrls == null) break;
                Map<String, Object> place = new HashMap<String, Object>();
                place.put("imageUrls", imageUrls);
                place.put("title", title);
                resultPlaces.add(place);
            }
        }

        // 검색시 DB에 트렌드 카운트 추가
        Optional<SearchTrend> optionalTrend = searchTrendRepository.findById(keyword);
        if (optionalTrend.isPresent()) {
            increaseSearchCnt(keyword);
        } else {
            searchTrendRepository.save(SearchTrend.builder().keyword(keyword).searchCount(1).build());
        }

        result.put("meta", newMeta);
        result.put("places", resultPlaces);

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(result);

        return jsonInString;
    }

    @Transactional
    public void increaseSearchCnt(String keyword) {
        SearchTrend searchTrend = searchTrendRepository.findWithKeywordForUpdate(keyword);
        searchTrend.increaseTrendCnt();
    }
}
