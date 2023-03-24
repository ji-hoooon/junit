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
