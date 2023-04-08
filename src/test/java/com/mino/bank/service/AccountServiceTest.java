package com.mino.bank.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.config.dummy.DummyObject;
import com.mino.bank.domain.Account;
import com.mino.bank.domain.User;
import com.mino.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import com.mino.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import com.mino.bank.repository.AccountRepository;
import com.mino.bank.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//필요한 환경만 띄워서 테스트하는 가짜 환경 테스트
@ExtendWith(MockitoExtension.class)
class AccountServiceTest extends DummyObject {
    @InjectMocks    //@Mock들이 주입되는 대상
    private AccountService accountService;  //@Mock 객체를 대신 주입해준다.

    @Mock
    private UserRepository userRepository;  //가짜 환경에 주입할 가짜 객체
    @Mock
    private AccountRepository accountRepository;  //가짜 환경에 주입할 가짜 객체

    @Spy
    private ObjectMapper om;

    @Test
    void 계좌등록_test() throws Exception{
        //given
        //: 계좌 등록시 사용할 유저 아이디 명시
        Long userId = 1L;
        AccountSaveReqDto accountSaveReqDto=new AccountSaveReqDto();
        accountSaveReqDto.setNumber(1111L);
        accountSaveReqDto.setPassword(1234L);

        //stub
        //(1) findById
//        User user=newMockUser(1L, "ssar", "pepe ssar"); //id를 넣기 위해서는 newMockUser 필요
        User user=newMockUser(userId, "ssar", "pepe ssar"); //id를 넣기 위해서는 newMockUser 필요
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        //: DB에서 조회했을 때 해당 유저가 존재해야 가능하므로

        //(2) findByNumber
        when(accountRepository.findByNumber(any())).thenReturn(Optional.empty());
        //: DB에서 조회했을 때 해당 번호를 가진 계좌가 없어야 가능하므로

        //(3) save
        //id를 넣기 위해서는 newMockAccount 필요 -> newAccount 메서드와 newMockAccount 메서드 작성
        Account account=newMockAccount(1L,1111L,1000L,user);
        when(accountRepository.save(any())).thenReturn(account);

        //when
        AccountSaveRespDto accountSaveRespDto = accountService.계좌등록(accountSaveReqDto, userId);
        //RespDto를 JSON으로 확인
        String responseBody = om.writeValueAsString(accountSaveRespDto);
        System.out.println("테스트 : "+responseBody);

        //then
        Assertions.assertThat(accountSaveRespDto.getNumber()).isEqualTo(1111L);

    }
}