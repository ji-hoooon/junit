package com.mino.bank.controller;

import com.mino.bank.config.auth.LoginUser;
import com.mino.bank.dto.ResponseDto;
import com.mino.bank.dto.transaction.TransactionRespDto.TransactionListRespDto;
import com.mino.bank.service.AccountService;
import com.mino.bank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class TransactionController {
    private final TransactionService transactionService;
    private final AccountService accountService;

    @GetMapping("/s/account/{number}/transaction")
    //@RequestParam의 경우 기본값을 지정해야할 때만 사용하는 어노테이션
    public ResponseEntity<?> findTransactionList(@PathVariable Long number,
                                                 @RequestParam(value = "gubun", defaultValue = "ALL") String gubun,
                                                 @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                 @AuthenticationPrincipal LoginUser loginUser){
        //쿼리스트링, x-www-form-urlencoded는 모두 문자열로 들어오므로, 기본값 설정시에는 모두 문자열로 -> 자동 바인딩
        TransactionListRespDto transactionListRespDto= transactionService.입출금목록보기(loginUser.getUser().getId(), number,gubun,page);

//        return new ResponseEntity<>(new ResponseDto<>(1,"입출금목록보기 완료", transactionListRespDto), HttpStatus.OK);
        return ResponseEntity.ok(new ResponseDto<>(1,"입출금목록보기 완료", transactionListRespDto));
    }

}
