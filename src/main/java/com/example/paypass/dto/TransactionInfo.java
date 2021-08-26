package com.example.paypass.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;

import static com.example.paypass.util.JsonUtil.getMapperWithDateFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class TransactionInfo {
    private String userName;
    private String action;
    private TransactionDetail transactionDetail;
    private Date transactionDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Slf4j
    public static class TransactionDetail {
       private BigDecimal totalAmount;
       private List<TransactionRecord> transactionRecordList;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Slf4j
    public static class TransactionRecord {
        private  String receiver;
        private  BigDecimal amount;
    }
}


