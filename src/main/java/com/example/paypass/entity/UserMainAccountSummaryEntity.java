package com.example.paypass.entity;

import com.example.paypass.dto.AccountInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Entity
@Table(name = "USER_MAIN_ACCOUNT_SUMMARY")
public class UserMainAccountSummaryEntity {
    @GeneratedValue(strategy = IDENTITY)
    @Id
    private Integer id;

    @Column(name = "userName")
    private String userName;

    @Column(name = "deposits_balance")
    private BigDecimal depositsBalance;

    @Column(name = "outstanding_debt")
    private BigDecimal outstandingDebt; /* 1 -100: owing to others 100; 2 +100: owing from others */

    @Column(name = "creditor")
    private String creditor;

    @Column(name = "update_on")
    @UpdateTimestamp
    private Date updateOn;

//    @Column(name = "updated_by")
//    private String updatedBy;

    public AccountInfo toModel() {
        return AccountInfo.builder()
                .userName(userName)
                .balance(depositsBalance)
                .debt(outstandingDebt)
                .creditor(creditor).build();
    }
}


