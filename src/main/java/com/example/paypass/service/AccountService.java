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

    private final BigDecimal zero = BigDecimal.ZERO;

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
        validUserInfoForTopup(topupInfo);
        var payerMainAccountInfo = mainAccountSummaryRepo.findByUserName(topupInfo.getName());
        // 1 no debt
        if (payerMainAccountInfo.getOutstandingDebt().compareTo(BigDecimal.ZERO) >= 0) {
            return topupForNoDebtUser(payerMainAccountInfo, topupInfo);
        }
        // 2 has debt
        return topupForDebtUser(payerMainAccountInfo, topupInfo);
    }

    @Transient
    public AccountInfo payMoneyV2(PayInfo payInfo) {
        validUserInfoForPay(payInfo);
        var payerMainAccountInfo = mainAccountSummaryRepo.findByUserName(payInfo.getFromName());
        BigDecimal receivedAmount;
        BigDecimal receivedDebt;// should be negative;
        BigDecimal payBalance;
        BigDecimal payDebt;

        BigDecimal payerDebt = payerMainAccountInfo.getOutstandingDebt();
        BigDecimal payerBalance = payerMainAccountInfo.getDepositsBalance();
        if (isMoreThanZero(payerBalance)) {
            var payAmountLessThanOrEqualPayDebt = payInfo.getAmount().compareTo(payerDebt) <= 0;
            if (payAmountLessThanOrEqualPayDebt) {
                payBalance = payerBalance;
                payDebt = payerDebt.subtract(payInfo.getAmount());
                receivedAmount = zero;
                receivedDebt = payInfo.getAmount();
            } else {
                var payAmountLessThanOrEqualPayBalance = payInfo.getAmount().subtract(payerDebt).compareTo(payerBalance) <= 0;
                var remaining = payInfo.getAmount().subtract(payerDebt);
                if (payAmountLessThanOrEqualPayBalance) {
                    payBalance = payerBalance.subtract(remaining);
                    payDebt = zero;
                    receivedAmount = payInfo.getAmount().subtract(payerDebt.abs());
                    receivedDebt = payerDebt.abs();
                } else {
                    payBalance = zero;
                    payDebt = payerBalance.subtract(payInfo.getAmount());
                    receivedAmount = payerBalance;
                    receivedDebt = payDebt.abs();
                }
            }
        } else {
            throw new BusinessException("please return your debt firstly,thanks");
        }
        //update payer
        var updatedPayerMainAccountInfo = UserMainAccountSummaryEntity
                .builder()
                .id(payerMainAccountInfo.getId())
                .userName(payInfo.getFromName())
                .depositsBalance(payBalance)
                .outstandingDebt(payDebt)
                .creditor(getCreditorForPayer(payDebt, payerMainAccountInfo.getCreditor(), payInfo.getToName()))
                .build();
        mainAccountSummaryRepo.save(updatedPayerMainAccountInfo);

        //update receiver
        var receiverUpdatedAccountInfo = getReceiverUpdatedAccountInfoForPayV2(payInfo.getToName(), receivedAmount, receivedDebt);
        mainAccountSummaryRepo.save(receiverUpdatedAccountInfo);
        return updatedPayerMainAccountInfo.toModel();
    }

    private void validUserInfoForPay(PayInfo payInfo) {
        var payerMainAccountInfo = mainAccountSummaryRepo.findByUserName(payInfo.getFromName());
        if (!userService.isValidUser(payInfo.getToName()) || !userService.isValidUser(payInfo.getFromName())) {
            throw new BusinessException("cannot find this user");
        }

        if (isLessThanOrEqualToZero(payInfo.getAmount())) {
            throw new BusinessException("invalid amount in pay function!");
        }

        if (isLessThanOrEqualToZero(payerMainAccountInfo.getDepositsBalance())) {
            throw new BusinessException("please topup firstly,thanks");
        }

        if (payInfo.getToName().equals(payInfo.getFromName())) {
            throw new BusinessException("fromUser cannot be the same with toUser,thanks");
        }
    }

    private void validUserInfoForTopup(TopupInfo topupInfo) {
        if (!userService.isValidUser(topupInfo.getName())) {
            throw new BusinessException("cannot find this user");
        }


        if (isLessThanOrEqualToZero(topupInfo.getAmount())) {
            throw new BusinessException("invalid amount in topup function!");
        }
    }

    private String getCreditorForPayer(BigDecimal debt, String OriginalDebtor, String toUser) {
        if (isMoreThanZero(debt)) {
            return OriginalDebtor;
        } else if (debt.equals(zero)) {
            return "";
        } else {
            return toUser;
        }
    }

    private String getCreditorForReceiver(BigDecimal debt, String OriginalDebtor) {
        if (isLessThanZero(debt)) {
            return OriginalDebtor;
        } else {
            return "";
        }
    }

    private UserMainAccountSummaryEntity getReceiverUpdatedAccountInfoForPayV2(String receivedName,
                                                                               BigDecimal receivedAmount,
                                                                               BigDecimal receivedDebt) {
        var receiverMainAccountInfo = mainAccountSummaryRepo.findByUserName(receivedName);
        var receiverUpdatedBalance = receiverMainAccountInfo.getDepositsBalance().add(receivedAmount);
        var receiverUpdateDebt = receiverMainAccountInfo.getOutstandingDebt().add(receivedDebt);

        return UserMainAccountSummaryEntity.builder()
                .id(receiverMainAccountInfo.getId())
                .userName(receivedName)
                .depositsBalance(receiverUpdatedBalance)
                .outstandingDebt(receiverUpdateDebt)
                .creditor(getCreditorForReceiver(receiverUpdateDebt, receiverMainAccountInfo.getCreditor()))
                .build();
    }

    private AccountInfo topupForDebtUser(UserMainAccountSummaryEntity payerMainAccountInfo, TopupInfo topupInfo) throws JsonProcessingException {
        // update sender and receiver account
        var OriginalReceiver = payerMainAccountInfo.toModel().getCreditor();
        var payerRemainingAmount = payerMainAccountInfo.getOutstandingDebt().add(topupInfo.getAmount());
        var isEnoughForDebt = payerRemainingAmount.compareTo(zero) >= 0;
        var payerUpdatedBalance = isEnoughForDebt ?
                payerMainAccountInfo.getDepositsBalance().add(payerRemainingAmount) : payerMainAccountInfo.getDepositsBalance();
        var payerUpdatedDebt = isEnoughForDebt ? zero : payerRemainingAmount;
        var payerCreditor = isEnoughForDebt ? "" : payerMainAccountInfo.getCreditor();
        var updatedPayerMainAccountInfo = getUpdatedMainAccount(topupInfo.getName(), payerUpdatedBalance, payerUpdatedDebt, payerCreditor);
        mainAccountSummaryRepo.save(updatedPayerMainAccountInfo);

        // only for has debt with receive
        var receiverMainAccountInfo = mainAccountSummaryRepo.findByUserName(OriginalReceiver);
        var receivedDebt = payerMainAccountInfo.getOutstandingDebt().abs();
        var receiverUpdateDebt = isEnoughForDebt ? zero : receivedDebt;
        var receiverUpdateBalance = isEnoughForDebt ?
                receiverMainAccountInfo.getDepositsBalance().add(receiverMainAccountInfo.getOutstandingDebt()) : receiverMainAccountInfo.getDepositsBalance().add(topupInfo.getAmount());
        var updatedReceiverMainAccountInfo = UserMainAccountSummaryEntity.builder()
                .id(receiverMainAccountInfo.getId())
                .userName(OriginalReceiver)
                .depositsBalance(receiverUpdateBalance)
                .outstandingDebt(receiverUpdateDebt)
                .creditor(isEnoughForDebt ? "" : receiverMainAccountInfo.getCreditor())
                .build();
        mainAccountSummaryRepo.save(updatedReceiverMainAccountInfo);

        // add transaction to summary
        var topupName = topupInfo.getName();
        var transactionDetail = TransactionDetail.builder()
                .totalAmount(topupInfo.getAmount())
                .transactionRecordList(isEnoughForDebt ?
                        Arrays.asList(new TransactionRecord(topupInfo.getName(), payerRemainingAmount),
                                new TransactionRecord(OriginalReceiver, receiverUpdateBalance))
                        : List.of(new TransactionRecord(OriginalReceiver, topupInfo.getAmount()))
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

    private Boolean isLessThanOrEqualToZero(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    private Boolean isLessThanZero(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
}
