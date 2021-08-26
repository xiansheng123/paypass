package com.example.paypass.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.*;

public class UserInfoEntityTest {
    @Test
    public void toModel() {
        var updatedDate = new Date(1546214400000L);//"2018-12-31"
        var dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        var testUserInfo = UserInfoEntity.builder()
                .id(1)
                .userName("jack")
                .updatedBy("admin")
                .updateOn(updatedDate)
                .build().toModel();

        Assertions.assertThat(testUserInfo.getUserName()).isEqualTo("jack");
        Assertions.assertThat(dateFormat.format(testUserInfo.getUpdateOn())).isEqualTo("2018-12-31");
    }
}