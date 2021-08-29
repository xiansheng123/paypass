package com.example.paypass.controller;

import com.example.paypass.dto.PayInfo;
import com.example.paypass.dto.TopupInfo;
import com.example.paypass.service.AccountService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

@SpringBootTest
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private AccountService accountService;

    @Test
    void getUserByName() {
        var accInfo = accountService.findAccountByName("Alice1");
        Assertions.assertThat(accInfo.getUserName()).isEqualTo("Alice1");
    }

    @Test
    @DisplayName("Pay:amount < positiveDebt")
    void amountLessThanPositiveDebt() {
        var payInfo = PayInfo.builder()
                .fromName("Alice1")
                .toName("Bob1")
                .amount(new BigDecimal(30))
                .build();
        var payer = accountService.payMoneyV2(payInfo);
        Assertions.assertThat(payer.getUserName()).isEqualTo("Alice1");
        Assertions.assertThat(payer.getBalance()).isEqualTo(new BigDecimal("210.00"));
        Assertions.assertThat(payer.getDebt()).isEqualTo(new BigDecimal("10.00"));
        Assertions.assertThat(payer.getCreditor()).isEqualTo("");

        var receive = accountService.findAccountByName("Bob1");
        Assertions.assertThat(receive.getUserName()).isEqualTo("Bob1");
        Assertions.assertThat(receive.getBalance()).isEqualTo(new BigDecimal("0.00"));
        Assertions.assertThat(receive.getDebt()).isEqualTo(new BigDecimal("-10.00"));
        Assertions.assertThat(receive.getCreditor()).isEqualTo("Alice1");
    }

    @Test
    @DisplayName("Topup:amount > negativeDebt")
    void amountMoreThanPositiveDebt() throws JsonProcessingException {
        var topup = TopupInfo.builder()
                .name("Bob2")
                .amount(new BigDecimal(100))
                .build();
        var payer = accountService.topUpMoney(topup);
        Assertions.assertThat(payer.getUserName()).isEqualTo("Bob2");
        Assertions.assertThat(payer.getBalance()).isEqualByComparingTo(new BigDecimal("90.00"));
        Assertions.assertThat(payer.getDebt()).isEqualByComparingTo(new BigDecimal("0.00"));
        Assertions.assertThat(payer.getCreditor()).isEqualTo("");

        var receive = accountService.findAccountByName("Alice2");
        Assertions.assertThat(receive.getUserName()).isEqualTo("Alice2");
        Assertions.assertThat(receive.getBalance()).isEqualByComparingTo(new BigDecimal("220.00"));
        Assertions.assertThat(receive.getDebt()).isEqualByComparingTo(new BigDecimal("0.00"));
        Assertions.assertThat(receive.getCreditor()).isEqualTo("");
    }

}