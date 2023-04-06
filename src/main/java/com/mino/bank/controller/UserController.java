package com.mino.bank.controller;

import com.mino.bank.dto.ResponseDto;
import com.mino.bank.dto.user.UserReqDto;
import com.mino.bank.dto.user.UserRespDto;
import com.mino.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class UserController {
    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody @Valid UserReqDto.JoinReqDto joinReqDto, BindingResult bindingResult){    //유효성 검사
//    public void join(UserReqDto.JoinReqDto){    //기본전략이 x-www-urlencoded
//    public ResponseEntity<?> join(@RequestBody JoinReqDto joinReqDto){    //JSON
        //담긴 에러를 처리
        if(bindingResult.hasErrors()){
            //Map으로 담는다
            Map<String, String> errorMap  = new HashMap<>();
            for (FieldError error:bindingResult.getFieldErrors()) {
                errorMap.put(error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(new ResponseDto<>(-1, "유효성 검사 실패", errorMap), HttpStatus.BAD_REQUEST);
        }

        UserRespDto.JoinRespDto joinRespDto = userService.회원가입(joinReqDto);
        return new ResponseEntity<>(new ResponseDto<>(1, "회원가입 완료", joinRespDto), HttpStatus.CREATED);
    }
}
