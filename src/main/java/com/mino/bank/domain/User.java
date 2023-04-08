package com.mino.bank.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

//Audit 기능 사용하기 위한 어노테이션 1
@EntityListeners(AuditingEntityListener.class)
//Spring이 User 객체 생성시 빈생성자로 생성하기 때문에
@NoArgsConstructor
@Table(name = "user_tb")
@Entity
@Getter
//엔티티의 경우 Getter만 생성
public class User { //baseEntity 상속하는 경우 Junit 테스트에 어려움 존재
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = false, length =20)
    private String username;
    @Column(unique = false, length =60)
    //BCrypt로 인코딩시 늘어나기 때문에
    private String password;

    @Column(unique = false, length =20)
    private String email;
    @Column(unique = false, length =60)
    private String fullname;
    //Table에는 열거형이 존재하지 않으므로

    //Audit 기능 사용하기 위한 어노테이션 2
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserEnum role;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(Long id, String username, String password, String email, String fullname, UserEnum role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullname = fullname;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
