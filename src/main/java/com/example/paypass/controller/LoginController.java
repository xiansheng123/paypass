package com.example.paypass.controller;

import com.example.paypass.dto.LoginInfo;
import com.example.paypass.dto.UserInfo;
import com.example.paypass.exception.ValidationException;
import com.example.paypass.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {
    private final UserService userService;

    /*
     * By right we could verify user by token in header of request
     * But Currently we use sample encryption to implement it
     * */
    @PostMapping("/auth")
    public String authenticateLogin(@RequestBody LoginInfo loginInfo, HttpServletResponse response) {
        if (loginInfo.getUserName().isBlank() || loginInfo.getPassword().isBlank()) {
            throw new ValidationException("username:${loginInfo.userName}");
        }
        return "ok";
    }

    @PostMapping("/add")
    public UserInfo adduser(@RequestBody LoginInfo loginInfo) {
        return userService.createUser(loginInfo);
    }

    @GetMapping("/username/{name}")
    public UserInfo getUserByName(@PathVariable("name") String name) {
        return userService.findUserByName(name);
    }

    @GetMapping("/username/all")
    public List<UserInfo> getAllUser() {
        return userService.findAllUsers();
    }
}