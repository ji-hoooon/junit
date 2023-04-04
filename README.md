# Junit Bank App

### Jpa LocalDateTime 자동 생성
- @EnableJpaAuditing
- @EntityListeners(AuditingEntityListener.class)


```java
    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
```

### Enum 클래스 작성과 적용
-@Getter, @AllArgsConstructor, String 멤버변수 (Enum 클래스)
-@Enumerated(EnumType.String) (Enum 적용할 변수)
```java
@Getter
@AllArgsConstructor
public enum TransactionEnum {
    WITHDRAW("출금"), DEPOSIT("입금"), TRANSFER("이체"), ALL("입출금내역");

    private String value;
}
```

```java
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    //이넘을 인식할 수 있도록
    private TransactionEnum gubun;  //WITHDRAW, DEPOSIT, TRANSAFER, ALL 
```

### JUnit5 테스트에 영향을 주는 코드
```java
@Builder
@AllArgsContructor
//: 직접 풀 생성자 정의해서 @Builder 
@Slf4j
//: 직접 생성 private final Logger log = LoggerFactory.getLogger(getClass());
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
}
//: BaseEntity 상속하는 방식 말고 변수로 직접 생성
```


### 자동 빌드 설정 (IntelliJ)

- 1. build.gradle에 추가
```java
apply plugin: 'java'

tasks.withType(JavaCompile) {
options.encoding = 'UTF-8'
options.compilerArgs << "-parameters"
}

sourceCompatibility = 11
targetCompatibility = 11

```
- 2. Settings-Build, Execution, Deployment-Compiler-Build Project Automatically

- 3. Settings-Advanced Setting-Allow auto-make to start~

### 스프링 시큐리티

#### 필터 체인 설정 (1)
##### : CORS, CSRF, 인증 방식, JWT, Filter, 인증 체크, 권한 체크, 오류 제어
1. 보안성이 떨어지는 iframe, 인증시 팝업창을 띄우는 HttpBasic, JWT를 이용하기 위해 세션ID를 사용하지 않도록 설정
2. 초기 세팅시에는 CSRF 설정을 비활성화 CORS 설정 파일은 null로 설정 후 나중에 수정한다.
3. 스프링에서 제공하는 폼로그인 비활성화
4. authorizeRequests() 메서드 체인을 이용해 허용되는 요청 설정
   1. anyMatchers().authenticated() : 인증이 필요한 페이지의 uri 패턴 설정
   2. anyMatchers().hasRole() : 권한이 필요한 페이지의 uri 패턴 설정
   3. anyRequest().permitAll() : 그 외의 모든 요청은 허용


#### CORS 설정 (1)
##### : 허용할 요청에 대한 설정 작성
1. addAllowedHeader() : 허용할 헤더
2. addAllowedMethod() : 허용할 HTTP 메서드
3. addAllowedOriginPattern() : 허용할 IP 주소 (추후 프론트엔드 IP만 허용)
4. setAllowCredentials(true) : 클라이언트의 쿠키 요청 허용여부
5. UrlBasedCoresConfigurationSource 객체 생성해, registerCorsConfiguration() : cors 설정을 사용할 URI 패턴 지정해 CORS 레지스트리에 등록



## 테스트
### : 직접 테스트시 인증이 필요한 페이지에도 403 에러를 출력하는 문제 해결 필요

### (1) Mockito를 이용한 통합 테스트
#### @AutoConfigureMockMvc : 가짜 환경에 MockMvc가 등록 
#### @SpringBootTest(webEnvironment = WebEnvironment.MOCK) : 통합 테스트를 가짜 환경에서 수행하는 Mockito 테스트

```java
//가짜 환경에 MockMvc가 등록됨
@AutoConfigureMockMvc
//통합 테스트 수행
//: 가짜 환경에서 수행하는 Mockito 테스트
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class SecurityConfigTest {
    //가짜 환경에 등록된 MockMvc를 의존성 주입
    @Autowired
    private MockMvc mvc;

    @Test
    public void authentication_test() throws Exception{
        //given

        //when
        ResultActions resultActions=mvc.perform(MockMvcRequestBuilders.get(("/api/s/hello")));

        //웹, PostMan, 테스트에서 응답의 일관성을 유지하기 위해서 코드 변경 필요
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        int httpStatusCode = resultActions.andReturn().getResponse().getStatus();
        System.out.println("테스트 : "+responseBody);
        //:바디에 담기는 데이터가 없음
        System.out.println("테스트 : "+httpStatusCode);
        //:403출력

        //then
    }
```
### 응답의 일관성이 없는 문제 존재 -> 시큐리티에서 Exception 가로채서 응답을 만든다.
```java
        //응답의 일관성을 만들기 위해 Exception 가로채기
        http.exceptionHandling().authenticationEntryPoint(
                (request, response, authenticationException) ->{
                    //응답을 JSON으로 만들기
                    ObjectMapper objectMapper=new ObjectMapper();
                    ResponseDto<?> responseDto=new ResponseDto<>(-1, "권한 없음", null);
                    String responseBody = objectMapper.writeValueAsString(responseDto);

                    response.setContentType("application/json; charset=utf-8");
                    response.setStatus(403);
//                    response.getWriter().println("error");
                    response.getWriter().println(responseBody);
                    //공통적인 응답 DTO 작성 필요
                }
        );
```

### 인증이 필요한 페이지에서 403 에러가 나는 것을 직접 401 오류코드로 변경
### 권한이 필요한 페이지에서 403 에러로 처리하기 위해 uri 파싱해 admin인 경우에만 403 오류 처리
```java
public class CustomResponseUtil {
    private static final Logger log = LoggerFactory.getLogger(CustomResponseUtil.class);
    public static void unAuthentication(HttpServletResponse response, String msg){
        //파싱 오류가 날 경우 예외 처리
        try{
            //응답을 JSON으로 만들기
            ObjectMapper objectMapper=new ObjectMapper();
//            ResponseDto<?> responseDto=new ResponseDto<>(-1, "인증되지 않은 사용자", null);
            ResponseDto<?> responseDto=new ResponseDto<>(-1, msg, null);
            String responseBody = objectMapper.writeValueAsString(responseDto);

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(401);
            //response.getWriter().println("error");
            response.getWriter().println(responseBody);
            //공통적인 응답 DTO 작성 필요
        }catch (Exception e){
            log.error("서버 파싱 에러");

        }


    }
    public static void unAuthorization(HttpServletResponse response, String msg){
        //파싱 오류가 날 경우 예외 처리
        try{
            //응답을 JSON으로 만들기
            ObjectMapper objectMapper=new ObjectMapper();
//            ResponseDto<?> responseDto=new ResponseDto<>(-1, "권한이 없는 사용자", null);
            ResponseDto<?> responseDto=new ResponseDto<>(-1, msg, null);
            String responseBody = objectMapper.writeValueAsString(responseDto);

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(403);
            //response.getWriter().println("error");
            response.getWriter().println(responseBody);
            //공통적인 응답 DTO 작성 필요
        }catch (Exception e){
            log.error("서버 파싱 에러");

        }


    }
}
```
```java
//응답의 일관성을 만들기 위해 Exception 가로채기
        http.exceptionHandling().authenticationEntryPoint(

                (request, response, authenticationException) ->{
                    String uri = request.getRequestURI();
                    log.debug("디버그 : "+ uri);
                    if(uri.contains("admin")){
                        CustomResponseUtil.unAuthorization(response, "관리자만 접근이 가능합니다.");
                    }else{
                        CustomResponseUtil.unAuthentication(response, "로그인이 필요합니다.");
                    }

                }
        );
        //인증이 되지 않은 사용자에 대한 예외처리하는 메서드로 파라미터는
        // ExceptionTranslationFilter로 필터링 되는 AuthenticationEntryPoint 객체
        //: AuthenticationEntryPoint의 commence 메서드는 파라미터로 request, response, AuthenticationException
```