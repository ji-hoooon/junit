package com.mino.bank.service;

import com.mino.bank.domain.Account;
import com.mino.bank.domain.User;
import com.mino.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import com.mino.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import com.mino.bank.handler.ex.CustomApiException;
import com.mino.bank.repository.AccountRepository;
import com.mino.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    //DB의 변경에 요구되므로, 커밋이 진행된다.
    @Transactional
    public AccountSaveRespDto 계좌등록(AccountSaveReqDto accountSaveReqDto, Long userId){


        //(1) User 로그인 되어있는지 컨트롤러에서 체크하고 userId만 받는다.
        //:userId를 이용해 유저 엔티티 가져오기
//        User user=userRepository.findById(userId);
        //(2) 유저 엔티티를 이용해 DB에서 조회한다.
        //Optional 객체이므로,
        User user=userRepository.findById(userId).orElseThrow(
                //예외 발생시 (DB에 해당하는 유저가 없으면)
                ()->new CustomApiException("유저를 찾을 수 없습니다.")
        );
        //(3) 해당 계좌가 DB에 있는지 중복여부를 체크한다.
        //:findByNumber를 만들어서 해당 계좌번호가 존재하는지 체크
        Optional<Account> accountOP = accountRepository.findByNumber(accountSaveReqDto.getNumber());
        if(accountOP.isPresent()){
            //계좌 중복으로 인한 예외 던짐
            throw new CustomApiException("해당 계좌가 이미 존재합니다.");
        }
        //(4) 계좌를 등록
        //: AccountSaveReqDto의 toEntity메서드를 이용해 엔티티로 변환
        //AccountSaveReqDto: 계좌번호, 잔액, 밸런스, 유저 객체가 필요
        Account accountPS = accountRepository.save(accountSaveReqDto.toEntity(user));

        //(5) DTO를 응답한다.
        //AccountSaveRespDto :id, 계좌번호, 잔액 필요
        //: AccountSaveRespDto의 생성자로 Account를 전달해 생성
        return new AccountSaveRespDto(accountPS);
    }

}
