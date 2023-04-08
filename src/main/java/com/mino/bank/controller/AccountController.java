package com.mino.bank.controller;

import com.mino.bank.config.auth.LoginUser;
import com.mino.bank.dto.ResponseDto;
import com.mino.bank.dto.account.AccountReqDto;
import com.mino.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import com.mino.bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/s/account")
    public ResponseEntity<?> saveAccount(@RequestBody @Valid AccountReqDto.AccountSaveReqDto accountSaveReqDto, BindingResult bindingResult, @AuthenticationPrincipal LoginUser loginUser){
        AccountSaveRespDto accountSaveRespDto = accountService.계좌등록(accountSaveReqDto, loginUser.getUser().getId());
        return new ResponseEntity<>(new ResponseDto<>(1,"계좌 등록 완료", accountSaveRespDto),HttpStatus.CREATED);
    }
}

