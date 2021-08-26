package com.example.paypass.controller;

import com.example.paypass.dto.AccountInfo;
import com.example.paypass.dto.PayInfo;
import com.example.paypass.dto.TopupInfo;
import com.example.paypass.service.AccountService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/{name}")
    public AccountInfo getUserByName(@PathVariable("name") String name) {
        return accountService.findAccountByName(name);
    }

    @GetMapping("/all")
    // todo for test purpose, will be delete
    public List<AccountInfo> getUserByName() {
        return accountService.findAllAccount();
    }

    @PostMapping("/topup")
    public AccountInfo recharge(@RequestBody TopupInfo topupInfo) throws JsonProcessingException {
        return accountService.topUpMoney(topupInfo);
    }

    @PostMapping("/pay")
    public AccountInfo recharge(@RequestBody PayInfo payInfo) throws JsonProcessingException {
        return accountService.payMoney(payInfo);
    }
}