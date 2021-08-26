package com.example.paypass.entity;

import com.example.paypass.dto.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Entity
@Table(name = "USER_INFORMATION")
public class UserInfoEntity {
    @GeneratedValue(strategy = IDENTITY)
    @Id
    private Integer id;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "update_on")
    @UpdateTimestamp
    private Date updateOn;

    @Column(name = "updated_by")
    private String updatedBy;

    public UserInfo toModel() {
        return UserInfo.builder()
                .userName(userName)
                .updateOn(updateOn)
                .build();
    }
}

