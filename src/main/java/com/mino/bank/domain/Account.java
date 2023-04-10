package com.mino.bank.domain;

import com.mino.bank.handler.ex.CustomApiException;
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
//Spring이 Account 객체 생성시 빈생성자로 생성하기 때문에
@NoArgsConstructor
@Table(name = "account_tb")
@Entity
@Getter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 4)    //20->4
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

    /**
     * 서비스에서 계좌 삭제, 출금,이체를 위해
     * userId를 받아서 해당 계좌의 주인인지 체크하는 메서드
     * @param userId
     */
    public void checkOwner(Long userId){
//        String testUsername = user.getUsername();
//        System.out.println("테스트 : "+testUsername);
        //LAZY 로딩 발동할까? -> 발동해야 하지만 발동하지 않았음
        //: PC에 존재하면 가져옴 -> 1차 캐싱

//        if(user.getId()!=userId){
//            throw new CustomApiException("계좌 소유자가 아닙니다.");
//        }
        //: 127L이상 넘어가면 동등비교 실패

        if(user.getId().longValue()!=userId.longValue()){
            throw new CustomApiException("계좌 소유자가 아닙니다.");
        }
    }

    //계좌 무통장 입금 메서드
    public void deposit(Long amount) {
        balance+=amount;
    }

    //계좌 출금을 위해 필요한 3개의 메서드
    public void checkSamePassword(Long password) {
//        if(this.password!=password){
        //Long타입 비교하기 위해서는 .longValue()로 비교
        if(this.password.longValue()!=password.longValue()){
            throw new CustomApiException("계좌 비밀번호 검증에 실패했습니다.");
        }
    }

//    public void checkBalance(Long amount) {
//        if(this.balance<amount){
//            throw new CustomApiException("계좌 잔액이 부족합니다.");
//        }
//    }
//
//    public void withdraw(Long amount) {
//        balance-=amount;
//    }
    //: 안전하지 않은 코드 -> 리팩토링
    public void checkBalance(Long amount) {
        if(this.balance<amount){
            throw new CustomApiException("계좌 잔액이 부족합니다.");
        }
    }

    public void withdraw(Long amount) {
        checkBalance(amount);
        balance-=amount;
    }
}
