package com.mino.bank.repository;

import com.mino.bank.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
//    void findByUsername(String username);   //JPA의 쿼리메서드 발동
    //Null처리를 간단하게 해주는 Optional 객체
    //: select * from user where username= ?
    Optional<User> findByUsername(String username);   //JPA의 쿼리메서드 발동
}
