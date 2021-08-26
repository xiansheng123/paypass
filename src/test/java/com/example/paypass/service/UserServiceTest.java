package com.example.paypass.service;

import com.example.paypass.entity.UserInfoEntity;
import com.example.paypass.repository.UserInfoEntityRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserInfoEntityRepository userInfoEntityRepo;

    @InjectMocks
    private UserService UserService;

    @Test
    void isValidUser_Invalid() {
        when(userInfoEntityRepo.findAllByUserName(any())).thenReturn(Collections.emptyList());
        var result = UserService.isValidUser("admin");
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void isValidUser_Valid() {
        when(userInfoEntityRepo.findAllByUserName("admin"))
                .thenReturn(List.of(new UserInfoEntity(1, "admin", new Date(), "admin")));
        var result = UserService.isValidUser("admin");
        Assertions.assertThat(result).isTrue();
    }

}


