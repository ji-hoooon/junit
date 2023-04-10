package com.mino.bank.repository;

import com.mino.bank.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

//동적쿼리를 위한 DAO와 JpaRepository 상속
//public interface TransactionRepository extends JpaRepository<Transaction, Long> {
public interface TransactionRepository extends JpaRepository<Transaction, Long>, Dao {
    //인터페이스가 인터페이스를 구현할 때에는 implements 키워드 대신 extends 키워드를 사용하여 상속을 표현
    //인터페이스 끼리 상속할 때는 구현부가 없기 떄문에 extends 사용
 }
