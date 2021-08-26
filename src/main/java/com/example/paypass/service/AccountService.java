package com.example.paypass.service;

import com.example.paypass.dto.AccountInfo;
import com.example.paypass.dto.PayInfo;
import com.example.paypass.dto.TopupInfo;
import com.example.paypass.entity.TransactionSummaryEntity;
import com.example.paypass.entity.UserMainAccountSummaryEntity;
import com.example.paypass.exception.BusinessException;
import com.example.paypass.repository.TransactionSummaryRepository;
import com.example.paypass.repository.UserMainAccountSummaryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.paypass.dto.TransactionInfo.TransactionDetail;
import static com.example.paypass.dto.TransactionInfo.TransactionRecord;
import static com.example.paypass.util.JsonUtil.serializeToJsonString;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final UserService userService;
    private final UserMainAccountSummaryRepository mainAccountSummaryRepo;
    private final TransactionSummaryRepository transactionSummaryRepo;

    private final String ACTION_TOPUP = "Topup";
    private final String ACTION_PAY = "Pay";

    public AccountInfo findAccountByName(String name) {
        if (!userService.isValidUser(name)) {
            throw new BusinessException("cannot find this user");
        }
        return mainAccountSummaryRepo.findByUserName(name).toModel();
    }

    public List<AccountInfo> findAllAccount() {
        return mainAccountSummaryRepo.findAll().stream()
                .map(UserMainAccountSummaryEntity::toModel)
                .collect(Collectors.toList());
    }

    @Transient
    public AccountInfo topUpMoney(TopupInfo topupInfo) throws JsonProcessingException {
        var payerMainAccountInfo = mainAccountSummaryRepo.findByUserName(topupInfo.getName());
        // 1 no debt
        if (payerMainAccountInfo.getOutstandingDebt().compareTo(BigDecimal.ZERO) >= 0) {
            return topupForNoDebtUser(payerMainAccountInfo, topupInfo);
        }
        // 2 has debt
        return topupForDebtUser(payerMainAccountInfo, topupInfo);
    }

    @Transient
    public AccountInfo payMoney(PayInfo payInfo) throws JsonProcessingException {
        var payerMainAccountInfo = mainAccountSummaryRepo.findByUserName(payInfo.getFromName());
        if (payerMainAccountInfo.getDepositsBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("please topup firstly,thanks");
        }
        // 1 no debt
        if (payerMainAccountInfo.getOutstandingDebt().compareTo(BigDecimal.ZERO) >= 0) {
            return payWithoutDebt(payerMainAccountInfo, payInfo);
        }
        // 2 with debt
        return payWithDebt(payerMainAccountInfo, payInfo);
    }

    private AccountInfo payWithDebt(UserMainAccountSummaryEntity payerMainAccountInfo, PayInfo payInfo) throws JsonProcessingException {
        var payerRemaining = payerMainAccountInfo.getDepositsBalance().subtract(payInfo.getAmount());
        var payerUpdatedBalance = isMoreThanZero(payerRemaining) ? payerRemaining : BigDecimal.ZERO;
        var payerUpdatedDebt = isMoreThanZero(payerRemaining) ?
                BigDecimal.ZERO : payerRemaining.add(payerMainAccountInfo.getOutstandingDebt());
        var updatedPayerMainAccountInfo = UserMainAccountSummaryEntity
                .builder()
                .id(payerMainAccountInfo.getId())
                .userName(payInfo.getFromName())
                .depositsBalance(payerUpdatedBalance)
                .outstandingDebt(payerUpdatedDebt)
                .creditor(isMoreThanOrEqualToZero(payerUpdatedDebt) ? "" : payerMainAccountInfo.getCreditor())
                .build();
        mainAccountSummaryRepo.save(updatedPayerMainAccountInfo);

        var receivedAmount = payerUpdatedDebt.abs();
        var updatedReceiverMainAccountInfo = getReceiverUpdatedAccountInfoForPay(payInfo.getToName(), receivedAmount, BigDecimal.ZERO);
        mainAccountSummaryRepo.save(updatedReceiverMainAccountInfo);

        // add transaction to summary
        savePayTransactionSummary(payInfo);
        return updatedPayerMainAccountInfo.toModel();
    }

    private AccountInfo payWithoutDebt(
            UserMainAccountSummaryEntity payerMainAccountInfo,
            PayInfo payInfo) throws JsonProcessingException {
        var payerRemaining = payerMainAccountInfo.getDepositsBalance().subtract(payInfo.getAmount());
        var isBiggerThanZero = payerRemaining.compareTo(BigDecimal.ZERO) > 0;
        var payerUpdatedBalance = isBiggerThanZero ? payerRemaining : BigDecimal.ZERO;
        var payerUpdatedDebt = isBiggerThanZero ? BigDecimal.ZERO : payerRemaining;
        var updatedPayerMainAccountInfo = UserMainAccountSummaryEntity.builder()
                .id(payerMainAccountInfo.getId())
                .userName(payInfo.getFromName())
                .depositsBalance(payerUpdatedBalance)
                .outstandingDebt(payerUpdatedDebt)
                .creditor(isBiggerThanZero ? "" : payInfo.getToName())
                .build();
        mainAccountSummaryRepo.save(updatedPayerMainAccountInfo);

        var receivedBalance = payInfo.getAmount().subtract(payerUpdatedDebt.abs());
        var receivedDebt = payerUpdatedDebt.abs();
        var updatedReceiverMainAccountInfo = getReceiverUpdatedAccountInfoForPay(payInfo.getToName(), receivedBalance, receivedDebt);
        mainAccountSummaryRepo.save(updatedReceiverMainAccountInfo);

        // add transaction to summary
        savePayTransactionSummary(payInfo);
        return updatedPayerMainAccountInfo.toModel();
    }

    private UserMainAccountSummaryEntity getReceiverUpdatedAccountInfoForPay(String receivedName,
                                                                             BigDecimal receivedAmount,
                                                                             BigDecimal receivedDebt) {
        var receiverMainAccountInfo = mainAccountSummaryRepo.findByUserName(receivedName);
        var hasDebt = isLessThanZero(receiverMainAccountInfo.getOutstandingDebt());
        BigDecimal receiverUpdateDebt;
        BigDecimal receiverUpdatedBalance;
        if (hasDebt) {
            receiverUpdateDebt = receiverMainAccountInfo.getOutstandingDebt().add(receivedAmount);
            receiverUpdatedBalance = receiverUpdateDebt.compareTo(BigDecimal.ZERO) > 0 ?
                    receiverMainAccountInfo.getDepositsBalance().add(receiverUpdateDebt) : BigDecimal.ZERO;
        } else {
            receiverUpdatedBalance = receiverMainAccountInfo.getDepositsBalance().add(receivedAmount);
            receiverUpdateDebt = receiverMainAccountInfo.getOutstandingDebt().add(receivedDebt);
        }
        return UserMainAccountSummaryEntity.builder()
                .id(receiverMainAccountInfo.getId())
                .userName(receivedName)
                .depositsBalance(receiverUpdatedBalance)
                .outstandingDebt(receiverUpdateDebt)
                .creditor(isMoreThanOrEqualToZero(receiverUpdateDebt) ? "" : receiverMainAccountInfo.getCreditor()).build();
    }

    private void savePayTransactionSummary(PayInfo payInfo) throws JsonProcessingException {
        var transactionDetail = new TransactionDetail(payInfo.getAmount(),
                List.of(new TransactionRecord(payInfo.getToName(), payInfo.getAmount())));
        var payerRecord = TransactionSummaryEntity.builder()
                .fromUser(payInfo.getFromName())
                .toUser(payInfo.getToName())
                .action(ACTION_PAY)
                .transactionDetail(serializeToJsonString(transactionDetail)).build();

        transactionSummaryRepo.save(payerRecord);
    }

    private AccountInfo topupForDebtUser(UserMainAccountSummaryEntity payerMainAccountInfo, TopupInfo topupInfo) throws JsonProcessingException {
        // update sender and receiver account
        var payerRemainingAmount = payerMainAccountInfo.getOutstandingDebt().add(topupInfo.getAmount());
        var isEnoughForDebt = payerRemainingAmount.compareTo(BigDecimal.ZERO) >= 0;
        var payerUpdatedBalance = isEnoughForDebt ?
                payerMainAccountInfo.getDepositsBalance().add(payerRemainingAmount) : payerMainAccountInfo.getDepositsBalance();
        var payerUpdatedDebt = isEnoughForDebt ? BigDecimal.ZERO : payerRemainingAmount;
        var payerCreditor = isEnoughForDebt ? "" : payerMainAccountInfo.getCreditor();
        var updatedPayerMainAccountInfo = getUpdatedMainAccount(topupInfo.getName(), payerUpdatedBalance, payerUpdatedDebt, payerCreditor);
        mainAccountSummaryRepo.save(updatedPayerMainAccountInfo);

        var receiverName = payerMainAccountInfo.toModel().getCreditor();
        var receiveAmount = payerMainAccountInfo.getOutstandingDebt().abs();
        var receiverMainAccountInfo = mainAccountSummaryRepo.findByUserName(receiverName);
        var receiverUpdateDebt = isEnoughForDebt ? BigDecimal.ZERO : receiveAmount;
        var updatedReceiverMainAccountInfo = UserMainAccountSummaryEntity.builder()
                .id(receiverMainAccountInfo.getId())
                .userName(receiverName)
                .depositsBalance(receiverMainAccountInfo.getDepositsBalance())//no change
                .outstandingDebt(receiverUpdateDebt)
                .creditor(isEnoughForDebt ? "" : receiverMainAccountInfo.getCreditor()).build();
        mainAccountSummaryRepo.save(updatedReceiverMainAccountInfo);

        // add transaction to summary
        var topupName = topupInfo.getName();
        var transactionDetail = TransactionDetail.builder()
                .totalAmount(topupInfo.getAmount())
                .transactionRecordList(isEnoughForDebt ?
                        Arrays.asList(new TransactionRecord(topupInfo.getName(), payerRemainingAmount),
                                new TransactionRecord(receiverName, receiveAmount))
                        : List.of(new TransactionRecord(receiverName, topupInfo.getAmount()))
                ).build();

        var payerRecord = TransactionSummaryEntity.builder()
                .fromUser(topupName)
                .toUser(topupName)
                .action(ACTION_TOPUP)
                .transactionDetail(serializeToJsonString(transactionDetail))
                .build();

        transactionSummaryRepo.save(payerRecord);
        return updatedPayerMainAccountInfo.toModel();
    }

    private AccountInfo topupForNoDebtUser(
            UserMainAccountSummaryEntity payerMainAccountInfo,
            TopupInfo topupInfo) {
        // update sender
        var updatedBalance = payerMainAccountInfo.getDepositsBalance().add(topupInfo.getAmount());
        var updatedAccountInfo = getUpdatedMainAccount(
                payerMainAccountInfo.getUserName(),
                updatedBalance,
                payerMainAccountInfo.getOutstandingDebt());
        mainAccountSummaryRepo.save(updatedAccountInfo);

        // add transaction to summary
        var addedTransaction = TransactionSummaryEntity
                .builder()
                .fromUser(topupInfo.getName())
                .toUser(topupInfo.getName())
                .transactionDetail("{}")
                .action(ACTION_TOPUP).build();
        transactionSummaryRepo.save(addedTransaction);
        return updatedAccountInfo.toModel();
    }

    private UserMainAccountSummaryEntity getUpdatedMainAccount(String accName,
                                                               BigDecimal updatedBalance,
                                                               BigDecimal updateDebt) {
        var mainAccountInfo = mainAccountSummaryRepo.findByUserName(accName);
        return UserMainAccountSummaryEntity.builder()
                .id(mainAccountInfo.getId())
                .userName(accName)
                .depositsBalance(updatedBalance)
                .outstandingDebt(updateDebt)
                .creditor(mainAccountInfo.getCreditor())
                .build();
    }

    private UserMainAccountSummaryEntity getUpdatedMainAccount(String accName,
                                                               BigDecimal updatedBalance,
                                                               BigDecimal updateDebt,
                                                               String creditor) {
        var mainAccountInfo = getUpdatedMainAccount(accName, updatedBalance, updateDebt);
        mainAccountInfo.setCreditor(creditor);
        return mainAccountInfo;
    }

    private Boolean isMoreThanZero(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private Boolean isMoreThanOrEqualToZero(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    private Boolean isLessThanZero(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
}
