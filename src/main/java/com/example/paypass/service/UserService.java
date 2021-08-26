package com.example.paypass.service;

import com.example.paypass.dto.LoginInfo;
import com.example.paypass.dto.UserInfo;
import com.example.paypass.entity.UserInfoEntity;
import com.example.paypass.exception.BusinessException;
import com.example.paypass.exception.ValidationException;
import com.example.paypass.repository.UserInfoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserInfoEntityRepository userInfoEntityRepo;

    public UserInfo findUserByName(String name) {
        return userInfoEntityRepo.findByUserName(name).toModel();
    }

    public List<UserInfo> findAllUsers() {
        return userInfoEntityRepo.findAll().stream().map(UserInfoEntity::toModel).collect(Collectors.toList());
    }

    public Boolean isValidUser(String name) {
        if (name.isBlank()) {
            return false;
        }

        var list = userInfoEntityRepo.findAllByUserName(name);
        if (list.isEmpty()) {
            return false;
        }
        return true;
    }

    public UserInfo createUser(LoginInfo loginInfo) {
        if (userInfoEntityRepo.findByUserName(loginInfo.getUserName()) != null) {
            throw new BusinessException("this is existing user!");
        }
        var user = userInfoEntityRepo.save(
                UserInfoEntity.builder()
                        .userName(loginInfo.getUserName())
                        .updatedBy("admin")
                        .build()
        );
        return user.toModel();
    }
}