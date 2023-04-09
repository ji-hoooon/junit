package com.mino.bank.controller;

import com.mino.bank.config.auth.LoginUser;
import com.mino.bank.dto.ResponseDto;
import com.mino.bank.dto.account.AccountReqDto;
import com.mino.bank.dto.account.AccountReqDto.AccountDepositReqDto;
import com.mino.bank.dto.account.AccountRespDto.AccountDepositRespDto;
import com.mino.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import com.mino.bank.dto.account.AccountRespDto.AccountTransferRespDto;
import com.mino.bank.service.AccountService;
import com.mino.bank.service.AccountService.AccountListRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/s/account")
    public ResponseEntity<?> saveAccount(@RequestBody @Valid AccountReqDto.AccountSaveReqDto accountSaveReqDto, BindingResult bindingResult, @AuthenticationPrincipal LoginUser loginUser){
        AccountSaveRespDto accountSaveRespDto = accountService.계좌등록(accountSaveReqDto, loginUser.getUser().getId());
        return new ResponseEntity<>(new ResponseDto<>(1,"계좌 등록 완료", accountSaveRespDto), CREATED);
    }

    //리소스 요소 URI 패턴
    //: 단수명사를 사용하며 컬랙션의 개별요소를 나타낸다.
    //-> 해당 리소스 요소를 반환한다.
//    @GetMapping("/s/account/{id}")
//    //인증 필요, account 테이블에 1번 row 반환 요청
//    //-> 해당 유저 ID의 정보만 조회 가능하도록 권한 체크 절차 필요
//    public ResponseEntity<?> findUserAccount(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser){
//        //권한 체크
//        if(id!=loginUser.getUser().getId()){
//            throw new CustomForbiddenException("권한이 없습니다.");
//        }
//        //(1) 서비스 호출
//        AccountListRespDto accountListRespDto = accountService.계좌목록보기_유저별(id);
//        //(2) 응답DTO 반환
//        return new ResponseEntity<>(new ResponseDto<>(1,"계좌목록 보기_유저별 성공", accountListRespDto), OK);
//    }
    //: 로그인한 유저인데 꼭 권한 체크를 컨트롤러에서 해야하나? -> id를 받지말고 로그인한 유저의 정보만 출력할 수 없을까


    //인증이 필요하고, account 테이블에 login한 유저의 계좌 목록만 요청
    @GetMapping("/s/account/login-user")
    public ResponseEntity<?> findUserAccount(@AuthenticationPrincipal LoginUser loginUser){
        //(1) 서비스 호출
        AccountListRespDto accountListRespDto = accountService.계좌목록보기_유저별(loginUser.getUser().getId());
        //(2) 응답DTO 반환
        return new ResponseEntity<>(new ResponseDto<>(1,"계좌목록 보기_유저별 성공", accountListRespDto), OK);
    }
    @DeleteMapping("/s/account/{number}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long number, @AuthenticationPrincipal LoginUser loginUser){
        accountService.계좌삭제(number, loginUser.getUser().getId());

        return new ResponseEntity<>(new ResponseDto<>(1,"계좌 삭제 완료", null), HttpStatus.OK);
    }


    //인증이 필요없는 무통장입금메서드
    @PostMapping("/account/deposit")
    public ResponseEntity<?> depositAccount(@RequestBody @Valid AccountDepositReqDto accountDepositReqDto, BindingResult bindingResult){
        //BindingResult가 있어야 예외를 잡는다.
        AccountDepositRespDto accountDepositRespDto = accountService.계좌입금(accountDepositReqDto);
        return new ResponseEntity<>(new ResponseDto<>(1,"계좌 입금 완료", accountDepositRespDto), CREATED);
    }

    //인증이 필요한 계좌 출금메서드
    @PostMapping("/s/account/withdraw")
    public ResponseEntity<?> withdrawAccount(@RequestBody @Valid AccountReqDto.AccountWithdrawReqDto accountWithdrawReqDto, BindingResult bindingResult, @AuthenticationPrincipal LoginUser loginUser){
        //BindingResult가 있어야 예외를 잡는다.
        AccountTransferRespDto accountWithdrawRespDto = accountService.계좌출금(accountWithdrawReqDto, loginUser.getUser().getId());
        return new ResponseEntity<>(new ResponseDto<>(1,"계좌 출금 완료", accountWithdrawRespDto), CREATED);
    }


    //인증이 필요한 계좌 이체메서드
    @PostMapping("/s/account/transfer")
    public ResponseEntity<?> transferAccount(@RequestBody @Valid AccountReqDto.AccountTransferReqDto accountTransferReqDto, BindingResult bindingResult, @AuthenticationPrincipal LoginUser loginUser){
        //BindingResult가 있어야 예외를 잡는다.
        AccountTransferRespDto accountTransferRespDto = accountService.계좌이체(accountTransferReqDto, loginUser.getUser().getId());
        return new ResponseEntity<>(new ResponseDto<>(1,"계좌 이체 완료", accountTransferRespDto), CREATED);
    }
}

