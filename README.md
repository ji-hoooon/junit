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


### 회원가입
1. UserService에서 회원가입 메서드와 사용할 DTO 작성
2. 회원 가입 메서드 로직 
   3. 사용자 이름 중복체크
   4. 패스워드 인코딩 + 회원가입
   5. DTO 응답


1. 사용자 이름 중복체크
   2. UserRepository 의존성 주입
   3. 리포지토리에 findByUsername 메서드 작성
   4. 사용자이름 중복 발생시 예외를 처리하기 위한 핸들러 작성


### 회원가입 테스트
#### 모키토를 이용한 가짜 환경 테스트
1. 더미 오브젝트 만들기
2. @Mock으로 필요한 빈 주입하기
3. @InjectMock으로 의존성 주입
4. @Spy로 진짜 객체를 가짜 환경에 주입하기
```java
//서비스 테스트를 위한 모키토 환경은 스프링 관련 빈들이 존재하지 않는다.
@ExtendWith(MockitoExtension.class)
public class UserServiceTest extends DummyObject {

    //가짜 환경으로 주입하기 위한 어노테이션
    //: Mock으로 만든 가짜 객체와, Spy로 가져온 진짜 객체를 Inject한다.
    @InjectMocks
    private UserService userService;

    //모키토 환경에서는
    //빈의 직접 등록이 필요하므로 @Mock을 이용해 가짜로 메모리에 띄운다.
    @Mock
    private UserRepository userRepository;

    //진짜 객체를 가짜 객체로 집어넣는 방법
    @Spy
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    public void 회원가입_test() throws Exception{
        //given
        //요청 데이터 전달하기 위한 JoinReqDto 필요
        JoinReqDto joinReqDto = new JoinReqDto();
        joinReqDto.setUsername("ssar");
        joinReqDto.setPassword("1234");
        joinReqDto.setEmail("ssar@nate.com");
        joinReqDto.setFullname("pepe ssar");

        //stub1
        //가짜로 띄워줘도 해당 클래스의 메서드가 존재하지 않기 때문에, stub 처리 필요
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        //: 리포지토리의 중복 체크 메서드를 실행시, 빈 옵셔널 객체를 리턴한다.
//        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));
        //: 동일한 유저네임을 전달 리턴할 경우


        //stub2
        //user 객체를 리턴하는 스텁
        //: 패스워드 인코딩도 필요
//        User user= User.builder()
//                .id(1L)
//                .username("ssar")
//                .password("1234")
//                .email("ssar@nate.com")
//                .fullname("pepe ssar")
//                .role(UserEnum.CUSTOMER)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
        // 더미 오브젝트로 리팩토링
        User user = newMockUser(1L, "ssar", "pepe ssar");

        when(userRepository.save(any())).thenReturn(user);

        //when
        JoinRespDto joinRespDto = userService.회원가입(joinReqDto);
        System.out.println("테스트 : "+joinRespDto);
        //then


        assertThat(joinRespDto.getId()).isEqualTo(1L);
        assertThat(joinRespDto.getUsername()).isEqualTo("ssar");
    }
}
```

### 유효성 검사 수행
1. 	implementation 'org.springframework.boot:spring-boot-starter-validation' 의존성 주입
2. @NotEmpty와 같이 Validation 제약 조건 걸기
3. 검증 수행할 객체 앞에 @Valid 어노테이션 붙이고, 바로 뒤에 BindingResult로 에러들을 Map으로 받는다.

### 유효성 검사 AOP로 등록
1. 유효성 검사를 위한 Advice 클래스 작성
   1. 해당 클래스는 body가 존재하는 핸들러 메서드에서만 수행
   2. 따라서 @PostMapping, @PutMapping이 붙은 메서드에서만 수행
2. 유효성 검사 예외 클래스 작성
3. ExceptionHandler에 작성한 예외 클래스 등록
4. 이전에 작성한 유효성 검사 로직을 JointPoint 메서드 실행 전 후 제어하도록 @Around로 Advice 메서드 작성
```java
    //@Before, @After
    @Around("postMapping() || putMapping()")    //1. @PostMapping(), @PutMapping() 어노테이션이 붙은 모든 메서드에서
    //: joinPoint 메서드 실행 전 후 제어 가능한 어노테이션
    public Object validationAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();  //joinPoint의 매개변수
        for(Object arg: args){
            if(arg instanceof BindingResult){
                //2. 에러가 존재할 경우 -> 예외 던짐
                BindingResult bindingResult = (BindingResult) arg;
                //담긴 에러를 처리
                if(bindingResult.hasErrors()){
                    //Map으로 담는다
                    Map<String, String> errorMap  = new HashMap<>();
                    for (FieldError error:bindingResult.getFieldErrors()) {
                        errorMap.put(error.getField(), error.getDefaultMessage());
                    }
                    //return new ResponseEntity<>(new ResponseDto<>(-1, "유효성 검사 실패", errorMap), HttpStatus.BAD_REQUEST);
                    //유효성 검사 예외를 던진다.
                    throw new CustomValidationException("유효성검사 실패", errorMap);
                }
            }

        }
        //3. 에러가 존재하지 않을 경우 해당 메서드 정상 수행
        return proceedingJoinPoint.proceed();
    }
```

### JWT 인증과 인가 (1)
1. 토큰에 필요한 시크릿 키, 만료시간, 접두사, 헤더를 담은 JwtVO 작성
2. UserDetails를 구현한 LoginUser 클래스 작성
3. UserDetailsService를 구현한 LoginService 작성
4. 토큰 생성과 검증을 수행하는 JwtProcess 작성
5. 필터 작성 
   1. 필터 메서드 (1) attemptAuthentication 메서드: 로그인 인증 완료시 강제 로그인
      1. 로그인을 위한 DTO 작성 : UserReqDto의 내부클래스로 LoginReqDto (필터에서는 컨트롤러 전이므로 유효성 검사 불가능)
      2. 강제 로그인 : 토큰 방식의 인증을 사용하더라도 시큐리티의 권한체크, 인증체크 기능을 사용하기 위해 세션 생성 -> 임시 세션으로, request와 response 완료시 끝 
      3. 로그인 실패시 : 시큐리티가 가지고 있는 제어권을 가져오기 위해 InternalAuthenticationServiceException 예외를 던짐
   2. 필터 메서드 (2) successfulAuthentication 메서드 : 로그인 성공시 응답
      1. 파라미터의 authResult에서 로그인 유저 객체 얻기
      2. 얻은 로그인 유저로 토큰 생성
      3. 생성한 토큰을 헤더에 추가
      4. 로그인을 위한 응답 DTO 작성 후 loginUser를 이용해 loginRespDto 변환
      5. CustomResponseUtil에 JSON 응답 DTO 생성하는 메서드 작성 후 JSON 응답 DTO 반환
6. 부모의 메서드로 로그인 주소 변경
7. JWT 토큰 로그인 실패 처리 로직
```java
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        //cmd+option+T
        try {
            //(1) request 객체의 json 데이터 꺼내기
            ObjectMapper om =new ObjectMapper();
            //(2) 로그인을 위한 DTO 작성
            //: UserReqDto의 내부클래스로 LoginReqDto (필터에서는 컨트롤러 전이므로 유효성 검사 불가능)
            LoginReqDto loginReqDto=om.readValue(request.getInputStream(), LoginReqDto.class);

            //(3) 강제 로그인
            //: 토큰 방식의 인증을 사용하더라도 시큐리티의 권한체크, 인증체크 기능을 사용하기 위해 세션 생성
            //-> 임시 세션으로, request와 response 완료시 끝
            UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);    //UserDetailsService의 LoadUserByUsername 호출
            return authentication;

        } catch (Exception e) {
            //시큐리티 과정 중 예외이므로, authenticationEntryPoint에 걸린다.
            // : Spring Security에서 인증에 실패한 경우 처리를 담당하는 인터페이스
            //필터를 모두 통과한 후에 컨트롤러 단으로 들어가고, 그때 CustomExceptionHandler로 처리 가능하므로
            //authenticationEntryPoint에 걸리도록 InternalAuthenticationServiceException 예외를 던짐
            throw new InternalAuthenticationServiceException(e.getMessage());
        }
    }

```
```java
    //return authentication 잘 작동하면 successfulAuthentication 메서드 호출
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        //(1) 파라미터의 authResult에서 로그인 유저 객체 얻기
        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        //(2) 얻은 로그인 유저로 토큰 생성
        String jwtToken = JwtProcess.create(loginUser);
        //(3) 생성한 토큰을 헤더에 추가
        response.addHeader(JwtVO.HEADER, jwtToken);
        //로그인을 위한 응답 DTO 작성

        //(4) loginUser를 이용해 loginRespDto 변환
        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUser());

        //CustomResponseUtil에 JSON 응답 DTO 생성하는 메서드 작성

        //(5) JSON 응답 DTO 반환
        CustomResponseUtil.success(response, loginRespDto);
    }
```

```java
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/login");
        this.authenticationManager=authenticationManager;
    }

    //Post :/login시 동작하는 메서드
    //-> Post :/api/login시 동작하는 메서드
```

```java
    //로그인 실패 로직
    //시큐리티 과정 중 예외이므로, authenticationEntryPoint에 걸린다.
    // : Spring Security에서 인증에 실패한 경우 처리를 담당하는 인터페이스
    //시큐리티가 가지고 있는 제어권을 가져오기 위해 예외를 던짐
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        CustomResponseUtil.fail(response, "로그인 실패", HttpStatus.UNAUTHORIZED);
    }
```

### JWT 인증과 인가 (2)
1. 모든 주소에서 토큰 검증 수행하는 필터 작성
2. 접근 권한 페이지 요청시 예외 발생 후 응답하는 DTO 작성
   1. 시큐리티 설정에서 권한 실패시 예외 가로채기 설정
   2. CustomResponseUtil에 unauthentication메서드를 리팩토링해 fail 메서드 작성
   3. fail 메서드로 응답 DTO 작성
```java
/**
 * 모든 주소에서 동작하는 토큰 검증 필터
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
   public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
      super(authenticationManager);
   }

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
      if(isHeaderVerify(request, response)){
         //JWT 존재할 때
         //(1) 프로토콜로 인해 필요했던 Bearer 접두사 제거
         String token = request.getHeader(JwtVO.HEADER).replace(JwtVO.TOKEN_PREFIX, "");
         //(2) 토큰을 이용해 토큰 검증 수행해 로그인 유저 정보 얻는다.
         LoginUser loginUser = JwtProcess.verify(token);

         //(3) 강제로 임시 세션에 로그인한 유저로 유저의 토큰을 생성해 넣는다. (여기서 확인할 정보는 해당 유저의 권한정보)
         //UsernamePasswordAuthenticationToken의 파라미터는 로그인유저 객체 or username (null), 비밀번호 (null), 로그인한 유저의 권한
         //UsernamePasswordAuthenticationToken의 부모는 AbstractAuthenticationToken의 부모는 Authentication
         Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
         //(4) 강제 로그인 수행
         SecurityContextHolder.getContext().setAuthentication(authentication);
      }
      // 다음 필터 수행
      chain.doFilter(request,response);

   }

   //헤더에 JWT 있는지 체크하는 검증 메서드
   private boolean isHeaderVerify(HttpServletRequest request, HttpServletResponse response){
      String header = request.getHeader(JwtVO.HEADER);
      if(header==null || !header.startsWith(JwtVO.TOKEN_PREFIX)){
         return false;
      }
      return true;
   }
}

```

```java
    public static void fail(HttpServletResponse response, String msg, HttpStatus httpStatus){
        //파싱 오류가 날 경우 예외 처리
        try{
            //응답을 JSON으로 만들기
            ObjectMapper objectMapper=new ObjectMapper();
//            ResponseDto<?> responseDto=new ResponseDto<>(-1, "인증되지 않은 사용자", null);
            ResponseDto<?> responseDto=new ResponseDto<>(-1, msg, null);
            String responseBody = objectMapper.writeValueAsString(responseDto);

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(httpStatus.value());    //권한 없음 에러
            //response.getWriter().println("error");
            response.getWriter().println(responseBody);
            //공통적인 응답 DTO 작성 필요
        }catch (Exception e){
            log.error("서버 파싱 에러");

        }
    }
```

### JWT 인증과 인가 정리
1. 인증 처리 필터 : UsernamePasswordAuthenticationFilter를 상속한 클래스
   1. /api/login 요청 일때만 필터링
   2. username,password 받기
   3. 파싱해서 LoginReqDto로 변환
   4. 인증 토큰 생성
   5. 토큰을 이용해 DB에서 유저 확인 (authenticationManager의 authentication 메서드 이용)
      6. 유저가 DB에 존재하면, UsserDetailsService의 loadUserByUsername 자동 호출
      7. 유저가 DB에 존재하지 않으면, 필터에서는 @RestControllerAdvice 사용이 불가능하므로 InternalAuthenticationServiceException 발동시킨다.
      8. InternalAuthenticationServiceException가 unsuccessAuthentication 호출
   9. 인증 성공했으므로, 토큰을 이용해 LoginUser 객체 생성해 리턴한다.
   10. 리턴 받은 로그인 유저 객체를 시큐리티 전용 세션인 SecurityContextHolder에 담아서 강제 로그인 수행
   11. authentication 객체의 principal 변수로 로그인 유저가 담긴다.
   12. 로그인 성공하면 successfulAuthentication을 호출하는데
       13. 토큰 생성
       14. 토큰을 응답 헤더에 담는다.
       15. 응답을 리턴
   16. 이후 응답이 되면 SessionCreationPoilicy.STATELESS설정으로 인해 세션이 초기화된다.
2. 인가 : BasicAuthenticationFilter를 상속한 클래스
   1. 모든 요청을 필터링한다.
   2. 토큰이 있는지 확인한다.
   3. 토큰의 헤더에 Bearer부분을 자른다.
   4. 토큰 검증을 수행한다.
   5. 토큰 검증 성공시 얻은 로그인 유저 객체를 이용해 Authentication 객체를 직접 생성한다.
   6. Authentication 객체의 role 정보를 활용하기 위해서 임시세션영역인 SecurityContextHolder에 Authentication을 저장한다.
   7. 저장한 LoginUser의 id와 role을 이용해 요청 페이지의 인증과 권한 체크한다.
   8. 이후 응답이 되면 SessionCreationPoilicy.STATELESS설정으로 인해 세션이 초기화된다.

### JWT 인증과 인가 테스트
1. JwtProcessTest
```java
package com.mino.bank.config.jwt;

import com.mino.bank.config.auth.LoginUser;
import com.mino.bank.domain.user.User;
import com.mino.bank.domain.user.UserEnum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProcessTest {

    @Test
    void create_test() {
        //given
        //(1) 테스트에 사용할 유저 객체 생성
        User user = User.builder()
                .id(1L)
                .role(UserEnum.CUSTOMER)
                .build();
        LoginUser loginUser = new LoginUser(user);

        //when
        //(2) 테스트 진행하기 위해 토큰 생성
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println("테스트 : "+jwtToken);

        //then
        //(3) 토큰은 생성시마다 값이 바뀌기 때문에, Token에 Bearer가 붙어있는지만 체크
        assertThat(jwtToken.startsWith(JwtVO.TOKEN_PREFIX));
    }

    @Test
    void verify_test() {
        //given
        //(1) 토큰 검증을 위해 Bearer를 제외한 토큰 값을 가져옴
        String jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYW5rIiwicm9sZSI6IkNVU1RPTUVSIiwiaWQiOjEsImV4cCI6MTY4MTUzNzg1M30.8yhovOveayIJN-bCnFKx7ucQRbP0FVH8gLo9tD9a0HG0F2PKZu1cl6RWazwtMVi59ENh_Krve1xQGqFMBBANXA";

        //when
        //(2) 토큰 검증 후, 리턴값을 LoginUser 객체로 저장
        LoginUser loginUser = JwtProcess.verify(jwtToken);
        System.out.println("테스트 : "+loginUser.getUser().getId());
        System.out.println("테스트 : "+loginUser.getUser().getRole().name());

        //then
        Assertions.assertThat(loginUser.getUser().getId()).isEqualTo(1L);
        Assertions.assertThat(loginUser.getUser().getRole()).isEqualTo(UserEnum.CUSTOMER);
    }
}
```
2.JwtAuthenticationFilterTest
```java
package com.mino.bank.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.config.dummy.DummyObject;
import com.mino.bank.domain.user.UserRepository;
import com.mino.bank.dto.user.UserReqDto.LoginReqDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//각각의 테스트 메서드가 실행이 끝나면 롤백이 진행된다.
//: 테스트 코드에서는 롤백이 진행, 본 코드에서는 커밋이 진행
@Transactional
//작성해둔 프로퍼티 설정을 적용하기 위한 어노테이션
// '[org.hibernate.type]': TRACE 적용
//: 쿼리에 들어가는 값까지 확인이 가능
@ActiveProfiles("test")
//가짜 환경으로 스프링에 있는 컴포넌트들을 스캔해서 빈으로 등록
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
//MockMvc를 모키토환경에서 사용하기 위한 어노테이션
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest extends DummyObject {
    @Autowired
    private ObjectMapper om;
    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp(){
        //테스트를 위해 데이터 셋업
        //(1) UserRepository 의존성 주입
        //(2) extends DummyObject
        //(3) 유저 객체를 DB에 삽입
        userRepository.save(newUser("ssar", "pepe ssar"));
    }
    @Test
    void successfulAuthentication_test() throws Exception {
        //given
        //(1) request, response 데이터를 받아 getInputStream()으로 JSON 데이터 파싱
        //: 파싱 결과로 받은 LoginReqDto가 given데이터로 바디에 담겨온다.
        //-> ObjectMapper를 의존성 주입
        LoginReqDto loginReqDto = new LoginReqDto();
        //실제로 사용하지 않을 생성자를 굳이 테스트를 위해서 만들어서 작성하지말것
        loginReqDto.setUsername("ssar");
        loginReqDto.setPassword("1234");

        //(2) ObjectMapper로 LoginReqDto를 JSON으로 변환
        String requestBody = om.writeValueAsString(loginReqDto);
        System.out.println("테스트 : "+requestBody);

        //when
        //(3) 강제 로그인 부분은 UserDetailsService의 LoadUserByUsername 실행되는 부분이므로 Post 요청을 수행해서 테스트
        //: 가짜환경에서 요청을 수행한 결과를 ResultActions에 담는다.
        //가짜환경의 요청에는 HTTP메서드, 컨텐트, 컨텐트타입을 명시해야한다.
        ResultActions resultActions = mvc.perform(post("/api/login").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        //(4) 요청에 대한 리턴에서 응답을 얻어서 내용을 문자열로 만들어서 responseBody에 담는다.
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);
        //현재 DB에 해당 유저가 존재하지 않으므로, 테스트 실패 -> @BeforeEach나, @SetUp으로 미리 만들어준다.

        //(5) CustomResponseUtil.success 호출 완료 -> 헤더 확인 필요
        String jwtToken = resultActions.andReturn().getResponse().getHeader(JwtVO.HEADER);
        System.out.println("테스트 : "+jwtToken);

        //then
        //(6) HttpStatus 확인해 200인지 확인
        resultActions.andExpect(status().isOk());

        //(7) 토큰을 확인해 null이 아닌지 확인
        assertNotNull(jwtToken);
        //(8) 토큰을 확인해 접두사 확인 (Bearer)
        assertTrue(jwtToken.startsWith(JwtVO.TOKEN_PREFIX));
        //(9) JSON 데이터의 데이터 객체를 까봐서 username 키의 value를 확인
        resultActions.andExpect(jsonPath("$.data.username").value("ssar"));
    }

    @Test
    void unsuccessfulAuthentication_test() throws Exception {
        //given
        //(1) request, response 데이터를 받아 getInputStream()으로 JSON 데이터 파싱
        //: 파싱 결과로 받은 LoginReqDto가 given데이터로 바디에 담겨온다.
        //-> ObjectMapper를 의존성 주입
        LoginReqDto loginReqDto = new LoginReqDto();
        //실제로 사용하지 않을 생성자를 굳이 테스트를 위해서 만들어서 작성하지말것
        loginReqDto.setUsername("ssar");
        loginReqDto.setPassword("12345");   //비밀번호 오류

        //(2) ObjectMapper로 LoginReqDto를 JSON으로 변환
        String requestBody = om.writeValueAsString(loginReqDto);
        System.out.println("테스트 : "+requestBody);

        //when
        //(3) 강제 로그인 부분은 UserDetailsService의 LoadUserByUsername 실행되는 부분이므로 Post 요청을 수행해서 테스트
        //: 가짜환경에서 요청을 수행한 결과를 ResultActions에 담는다.
        //가짜환경의 요청에는 HTTP메서드, 컨텐트, 컨텐트타입을 명시해야한다.
        ResultActions resultActions = mvc.perform(post("/api/login").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        //(4) 요청에 대한 리턴에서 응답을 얻어서 내용을 문자열로 만들어서 responseBody에 담는다.
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);
        //현재 DB에 해당 유저가 존재하지 않으므로, 테스트 실패 -> @BeforeEach나, @SetUp으로 미리 만들어준다.

        //(5) CustomResponseUtil.success 호출 완료 -> 헤더 확인 필요
        String jwtToken = resultActions.andReturn().getResponse().getHeader(JwtVO.HEADER);
        System.out.println("테스트 : "+jwtToken);


        //then
        //(6) 로그인 실패 - 인증 실패, 파싱 실패 : InternalAuthenticationServiceException
        //-> HttpStatus가 401인지 확인
        resultActions.andExpect(status().isUnauthorized());
    }
}
```
3. JwtAuthorizationFilterTest
```java
package com.mino.bank.config.jwt;

import com.mino.bank.config.auth.LoginUser;
import com.mino.bank.domain.user.User;
import com.mino.bank.domain.user.UserEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


//작성해둔 프로퍼티 설정을 적용하기 위한 어노테이션
// '[org.hibernate.type]': TRACE 적용
//: 쿼리에 들어가는 값까지 확인이 가능
@ActiveProfiles("test")
//가짜 환경으로 스프링에 있는 컴포넌트들을 스캔해서 빈으로 등록
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
//MockMvc를 모키토환경에서 사용하기 위한 어노테이션
@AutoConfigureMockMvc
class JwtAuthorizationFilterTest {
    @Autowired
    private MockMvc mvc;
    @Test
    void authorization_success_test() throws Exception {
        //given
        //(1) 권한 체크를 위한 유저 객체 생성
        User user = User.builder()
                .id(1L)
                .role(UserEnum.CUSTOMER)
                .build();
        LoginUser loginUser = new LoginUser(user);
        //(2) 로그인 유저 객체를 이용해 JWT 직접 생성
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println("테스트 : "+jwtToken);

        //when
        //(3) 인증이 필요하지만, 없는 페이지 요청
//        ResultActions resultActions = mvc.perform(get("/api/s/hello/test"));

        //(3) 인증이 필요하지만, jwt을 담은 헤더를 담아서 없는 페이지 요청
        ResultActions resultActions = mvc.perform(get("/api/s/hello/test").header(JwtVO.HEADER, jwtToken));

        //then
        //(4) 404에러 발생 예상
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void authorization_fail_test() throws Exception {
        //given

        //when
        //(1) 인증이 필요하지만, 토큰 없이 페이지 요청
        ResultActions resultActions = mvc.perform(get("/api/s"));

        //then
        //(2) 인증이 필요한 401에러 발생 예상
        resultActions.andExpect(status().isUnauthorized());

    }

    @Test
    void authorization_admin_test() throws Exception {
        //given
        //(1) 권한 체크를 위한 유저 객체 생성
        User user = User.builder()
                .id(1L)
                .role(UserEnum.CUSTOMER)
                .build();
        LoginUser loginUser = new LoginUser(user);
        //(2) 로그인 유저 객체를 이용해 JWT 직접 생성
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println("테스트 : "+jwtToken);

        //when
        //(3) 인증이 필요하지만, 없는 페이지 요청
//        ResultActions resultActions = mvc.perform(get("/api/s/hello/test"));

        //(3) 인증이 필요하지만, jwt을 담은 헤더를 담아서 admin권한이 필요한 페이지 요청
        ResultActions resultActions = mvc.perform(get("/api/admin/hello/test").header(JwtVO.HEADER, jwtToken));

        //then
        //(4) 403에러 발생 예상 - 권한이 없음
        resultActions.andExpect(status().isForbidden());
    }
    
}
```
