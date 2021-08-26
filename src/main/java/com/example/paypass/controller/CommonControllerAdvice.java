package com.example.paypass.controller;

import com.example.paypass.dto.exception.ApiError;
import com.example.paypass.exception.BusinessException;
import com.example.paypass.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
@Slf4j
class CommonControllerAdvice {
    private static final String AUTH_ERROR = "your bankID and/or password is incorrect. please try again";
    private static final String LOGIC_ERROR = "please contact supporter, thanks";

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    @ExceptionHandler(ValidationException.class)
    public ApiError authException(ValidationException e) {
        log.error("Validation Exception:", e);
        return ApiError.builder()
                .group(AUTH_ERROR)
                .content("auth error happened")
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(BusinessException.class)
    public ApiError businessException(BusinessException e) {
        log.error("Business Exception:", e);
        return ApiError.builder()
                .group(LOGIC_ERROR)
                .content("business error happened")
                .build();
    }
}

