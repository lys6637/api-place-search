# 스프링부트 기반의 외부API 연동 검색서비스

장소 검색을 통한 장소정보와 이미지 Url 제공, Top 10 인기 검색어 제공 

## 환경구성

개발언어 : JAVA8  
프레임워크 : Spring Boot 2.3.9.RELEASE  
빌드 : Gradle  
DB : H2  
Open API : Kakao API(키워드로 장소검색 / 이미지검색), Naver API(지역 검색 / 이미지검색)

## 카카오, 네이버 API 연동

검색 API를 사용하려면 카카오와 네이버 오픈 API키가 반드시 필요합니다. 발급방법은 
[카카오](https://developers.kakao.com/docs/latest/ko/getting-started/app) 와 [네이버](https://developers.naver.com/docs/common/openapiguide) 를 참고하세요.


### API키 적용 및 실행 방법

> src/main/resources/application.yml

위 경로 파일의 `{INPUT-YOUR-API-KEY}` 부분을 발급받은 키로 교체 후 다음 명령어를 실행합니다.


## 기능 사용하기
외부API 연동 검색서비스 장소검색과 인기키워드 검색 두 가지 기능을 제공합니다.

### IntelliJ 를 통한 기능 실행
프로젝트 루트에 위치한 `testRequest.http`를 통해 API 를 호출하여 기능을 사용할 수 있습니다.

### curl 을 통한 기능 실행
#### 장소 검색
```shell
$ curl "http://localhost:8080/place?keyword=kakao&pageSize=10&currentPage=1" 
```
keyword= 검색어입력(필수)  
pageSize= 한 페이지에 보여지는 데이터 갯수  
currentPage= 현재 페이지번호


#### Top 10 인기키워드 검색
기능 실행의 편의성을 위해 dummy 데이터를 쿼리를 통해 미리 입력해 놓았음

```shell
$ curl "http://localhost:8080/trend"
```


## 설계 및 구현상의 고려사항
- 대용량 트래픽으로 인한 외부 API 서비스 장애시 대응방안
    - 단일 API로 부터의 의존성 분산 : 카카오 API 장애시 네이버 API로 자동으로 연결되어 대응
    - 지속적인 API 장애 발생시 모니터링 및 대응 방안으로 Hystrix 서킷브레이커 사용
      1. 장애 발생시 Fallback 함수를 통해 대응하고, 지속적인 장애시 모니터링 후 Open상태로 변경하여 API를 호출하지 않고 바로 대응
      2. App이 작동중인 상태에서 기능 실행 후 API 연결상태를 [Dashboard](http://localhost/hystrix) 를 통해 지속 모니터링 가능 ( [Dashboard](http://localhost/hystrix) 에서 http://localhost:8080/actuator/hystrix.stream url 입력 후 접속 가능)

- 검색 랭킹 갱신
    - 동시성 문제가 발생할 수 있다.
    - JPA Pessimistic Lock을 통해서 트랜잭션 충돌괃 동시성 문제 해결
    

### 추가로 고려해 볼 사항 : API에 대한 부하가 매우 커졌을 경우 확장할 수 있는 방안
#### Load Balancer
- API에 대한 외부로부터의 요청이 많아져서 부하가 커졌을 경우, 서버의 Scale-out 후 Load Balancer를 이용한 트래픽 제어를 할 수 있다.
  Load Balancer는 각 서버들의 처리가능 능력, 응답시간, 트래픽 등을 고려해서 트랜잭션을 분배해 줄 수 있다.
  