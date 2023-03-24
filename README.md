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