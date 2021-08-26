package com.example.paypass.service;

import com.example.paypass.dto.TopupInfo;
import com.example.paypass.entity.TransactionSummaryEntity;
import com.example.paypass.entity.UserMainAccountSummaryEntity;
import com.example.paypass.repository.TransactionSummaryRepository;
import com.example.paypass.repository.UserMainAccountSummaryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private UserMainAccountSummaryRepository mainAccountSummaryRepo;

    @Mock
    private TransactionSummaryRepository transactionSummaryRepo;

    @InjectMocks
    private AccountService accountService;

    @Nested
    @DisplayName("topup function test")
    class topup {
        @Test
        public void topupForNoDebtUser() throws JsonProcessingException {
            var name = "Bob";
            var topupInfo = new TopupInfo(name, new BigDecimal(100));
            var zero = BigDecimal.ZERO;
            when(mainAccountSummaryRepo.findByUserName(name)).thenReturn(
                    UserMainAccountSummaryEntity.builder()
                            .id(1)
                            .userName(name)
                            .depositsBalance(zero)
                            .outstandingDebt(zero)
                            .creditor("")
                            .build());

            when(mainAccountSummaryRepo.save(any())).thenReturn(createMainAccount());
            when(transactionSummaryRepo.save(any())).
                    thenReturn(createTransactionSummaryEntity());


            var result = accountService.topUpMoney(topupInfo);
            Assertions.assertThat(result.getUserName()).isEqualTo(name);
            Assertions.assertThat(result.getBalance()).isEqualTo(new BigDecimal(100));
            Assertions.assertThat(result.getDebt()).isEqualTo(zero);
            Assertions.assertThat(result.getCreditor()).isEmpty();
        }

        @Test
        public void topupForDebtUser() throws JsonProcessingException {
            var name = "Bob";
            var topupInfo = new TopupInfo(name, new BigDecimal(100));
            var zero = BigDecimal.ZERO;
            var receiver = "Alice";
            when(
                    mainAccountSummaryRepo.findByUserName(name)
            ).thenReturn(
                    UserMainAccountSummaryEntity.builder()
                            .id(1)
                            .userName(name)
                            .depositsBalance(zero)
                            .outstandingDebt(new BigDecimal(-90))
                            .creditor(receiver)
                            .build()
            );
            when(mainAccountSummaryRepo.findByUserName(receiver)).thenReturn(
                    UserMainAccountSummaryEntity.builder()
                            .id(2)
                            .userName(receiver)
                            .depositsBalance(zero)
                            .outstandingDebt(new BigDecimal(90))
                            .creditor("")
                            .build()
            );
            when(mainAccountSummaryRepo.save(any())).thenReturn(createMainAccount());
            when(transactionSummaryRepo.save(any())).thenReturn(createTransactionSummaryEntity());
            var result = accountService.topUpMoney(topupInfo);
            Assertions.assertThat(result.getUserName()).isEqualTo(name);
            Assertions.assertThat(result.getBalance()).isEqualTo(new BigDecimal(10));
            Assertions.assertThat(result.getDebt()).isEqualTo(zero);
            Assertions.assertThat(result.getCreditor()).isEmpty();
        }

        private UserMainAccountSummaryEntity createMainAccount() {
            var zero = BigDecimal.ZERO;
            return UserMainAccountSummaryEntity.builder()
                    .id(1)
                    .userName("Bob")
                    .depositsBalance(zero)
                    .outstandingDebt(zero)
                    .build();
        }

        private TransactionSummaryEntity createTransactionSummaryEntity() {
            return TransactionSummaryEntity.builder()
                    .id(1)
                    .build();
        }
    }

}

