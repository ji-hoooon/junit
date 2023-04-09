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
    //: user 객체가 지연로딩이므로 함께 땡겨오도록 fetch join
//    @Query ("SELECT AC FROM ACCOUNT AC JOIN FETCH AC.USER U WHERE AC.NUMBER= :NUMBER")
    //: join fetch를 하면 조인해서 객체에 값을 미리 가져올 수 있다.

    //하지만 여기서는 id만 필요하므로, 굳이 할 필요 없음
    Optional<Account> findByNumber(Long number);

    //로그인한 유저의 계좌 목록만 보는 메서드
    //select * from account where user_id=:id;
    List<Account> findByUser_id(Long id);
}
