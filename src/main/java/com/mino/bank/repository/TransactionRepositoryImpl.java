package com.mino.bank.repository;

import com.mino.bank.domain.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

//동적쿼리를 위한 DAO
interface Dao{
    //파라미터 하나면 @Param이 필요없지만
    //파라미터 여러개면 어노테이션이 필요하다.
    List<Transaction> findTransactionList(@Param("accountId") Long accountId,@Param("gubun") String gubun, @Param("page") Integer page);
    //: 모두 @Param 붙어있는지 꼭 확인 -> 파람 어노테이션이 없어도 에러라고 출력하지 않음
}

@RequiredArgsConstructor
//인터페이스를 구현한 클래스
public class TransactionRepositoryImpl implements Dao{
    private final EntityManager em;

    /**
     * 1. JPQL이란
     * 2. join fetch란
     * 3. outer join이란
     * @param accountId
     * @param gubun
     * @param page
     * @return
     */

    @Override
    public List<Transaction> findTransactionList(Long accountId, String gubun, Integer page) {
        //gubun값을 이용해서 동적쿼리 작성한다.

        //JPQL으로 쿼리 작성
        String sql="";
        sql +="select t from Transaction t ";

        //fetch join
        if(gubun.equals("WITHDRAW")){
            sql+="join fetch t.withdrawAccount wa ";
            sql+="where t.withdrawAccount.id =: withdrawAccountId ";
        }else if(gubun.equals("DEPOSIT")){
            sql+="join fetch t.depositAccount da ";
            sql+="where t.depositAccount.id =: depositAccountId ";
        }else{  //gubun=ALL
            //입출력 : 아우터 조인 필요
            sql+="left join t.withdrawAccount wa ";
            sql+="left join t.depositAccount da ";
            sql+="where t.withdrawAccount.id =: withdrawAccountId ";
            sql+="or ";
            sql+="t.depositAccount.id =: depositAccountId";
        }

        //createQuery - JPQL, createNativeQuery - SQL
        TypedQuery<Transaction> query=em.createQuery(sql, Transaction.class);

        if(gubun.equals("WITHDRAW")){
            query=query.setParameter("withdrawAccountId", accountId);
        }
        else if(gubun.equals("DEPOSIT")){
            query=query.setParameter("depositAccountId", accountId);
        }else{
            query=query.setParameter("withdrawAccountId", accountId);
            query=query.setParameter("depositAccountId", accountId);
        }
        query.setFirstResult(page*5);
        //page = 0, 0, 5, 10, -> 5, 10, 15
        query.setMaxResults(5);

        return query.getResultList();
    }
}
