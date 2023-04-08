package com.mino.bank.repository;

import com.mino.bank.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * 계좌번호로 계좌 검색하는 메서드
     * @param number
     * @return
     */
    //select * from account where number = :number
    //checkpoint: 리팩토링 필요 -> user 객체가 지연로딩이므로 함께 땡겨오도록 fetch join?
    Optional<Account> findByNumber(Long number);
}
