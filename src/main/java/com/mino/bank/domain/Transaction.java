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
//Spring이 transaction 객체 생성시 빈생성자로 생성하기 때문에
@NoArgsConstructor
@Table(name = "transaction_tb")
@Entity
@Getter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //이체 정보
    //출금 정보
    //입금 정보
    @ManyToOne (fetch=FetchType.LAZY)
    //cascade 삭제를 위한 제약조건 해제
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Account withdrawAccount;
    @ManyToOne (fetch=FetchType.LAZY)
    //cascade 삭제를 위한 제약조건 해제
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Account depositAccount;

    private Long amount;

    //입금 출금시 트랜잭션의 히스토리를 가져온다.
    private Long withdrawAccountBalance;
    private Long depositAccountBalance;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    //이넘을 인식할 수 있도록
    private TransactionEnum gubun;  //WITHDRAW, DEPOSIT, TRANSAFER, ALL

    //계좌를 삭제할 때에도 로그를 남아야 한다.
    //: 일반적으로 휴면계좌인지 아닌지 상태코드로 남긴다. (여기서는 Junit테스트를 위해 삭제)
    private String sender;
    private String receiver;
    private String tel;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    @Builder
    public Transaction(Long id, Account withdrawAccount, Account depositAccount, Long amount, Long withdrawAccountBalance, Long depositAccountBalance, TransactionEnum gubun, String sender, String receiver, String tel, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.withdrawAccount = withdrawAccount;
        this.depositAccount = depositAccount;
        this.amount = amount;
        this.withdrawAccountBalance = withdrawAccountBalance;
        this.depositAccountBalance = depositAccountBalance;
        this.gubun = gubun;
        this.sender = sender;
        this.receiver = receiver;
        this.tel = tel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
