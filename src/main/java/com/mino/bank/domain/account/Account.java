package com.mino.bank.domain.account;

import com.mino.bank.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.util.Lazy;

import javax.persistence.*;
import java.time.LocalDateTime;

//Audit 기능 사용하기 위한 어노테이션 1
@EntityListeners(AuditingEntityListener.class)
//Spring이 User 객체 생성시 빈생성자로 생성하기 때문에
@NoArgsConstructor
@Table(name = "account_tb")
@Entity
@Getter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private Long number;
    @Column(nullable = false, length = 4)
    private Long password;
    @Column(nullable = false)
    private Long balance;   //잔액 기본값 1000

    //항상 ORM에서 FK의 주인(연관관계의 주인)은 Many Entity 쪽이다.
    //: 기본 전략을 지연로딩으로 변경
    //-> account 조회시, id를 통해 조인하는 시점이
    // account.getUser()일 때도 안하고
    //-> account.getUser().멤버 호출시에 lazy 발동하므로, 제어권을 사용자가 갖는다.
    @ManyToOne (fetch=FetchType.LAZY)
    private User user;  //컬럼명 기본값 : user_id

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Account(Long id, Long number, Long password, Long balance, User user, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.number = number;
        this.password = password;
        this.balance = balance;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
