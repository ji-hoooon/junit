package com.mino.bank.repository;

import com.mino.bank.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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

    //로그인한 유저의 계좌 목록만 보는 메서드
    //select * from account where user_id=:id;
    List<Account> findByUser_id(Long id);
}
