package com.mino.bank.service;

import com.mino.bank.domain.Account;
import com.mino.bank.domain.Transaction;
import com.mino.bank.dto.transaction.TransactionRespDto.TransactionListRespDto;
import com.mino.bank.handler.ex.CustomApiException;
import com.mino.bank.repository.AccountRepository;
import com.mino.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionListRespDto 입출금목록보기(Long userId, Long accountNumber, String gubun, Integer page){
        //(1) 계좌 존재 여부 체크
        Account accountPS = accountRepository.findByNumber(accountNumber).orElseThrow(
                () -> new CustomApiException("해당 계좌를 찾을 수 없습니다.")
        );
        //(2) 해당 계좌의 소유자 체크
        accountPS.checkOwner(userId);
        //: 유일하게 테스트해야하는 로직이지만, 이미 accountService에서 테스트했으므로 패스

        //(3) 입출금내역 출력을 위한 DTO 작성
        List<Transaction> transactionListPS = transactionRepository.findTransactionList(accountPS.getId(), gubun, page);

        return new TransactionListRespDto(accountPS,transactionListPS);
    }
}
