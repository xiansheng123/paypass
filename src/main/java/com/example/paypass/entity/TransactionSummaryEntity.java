package com.example.paypass.entity;

import com.example.paypass.dto.TransactionInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

import static com.example.paypass.util.JsonUtil.getMapperWithDateFormat;
import static javax.persistence.GenerationType.IDENTITY;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Entity
@Table(name = "TRANSACTION_SUMMARY")
public class TransactionSummaryEntity {

    @GeneratedValue(strategy = IDENTITY)
    @Id
    private Integer id;

    @Column(name = "from_user")
    private String fromUser;

    @Column(name = "to_user")
    private String toUser;

    @Column(name = "action")
    private String action;

    @Column(name = "transaction_detail")
    private String transactionDetail;

    @Column(name = "update_on")
    @UpdateTimestamp
    private Date updateOn;

//    @Column(name = "updated_by")
//    private String updatedBy;

    public TransactionInfo toModel() throws JsonProcessingException {
        return TransactionInfo.builder()
                .userName(fromUser)
                .action(action)
                .transactionDetail(getMapperWithDateFormat().readValue(transactionDetail, TransactionInfo.TransactionDetail.class))
                .transactionDate(updateOn)
                .build();
    }
}

