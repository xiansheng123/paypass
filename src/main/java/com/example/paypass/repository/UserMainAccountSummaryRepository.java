package com.example.paypass.repository;


import com.example.paypass.entity.UserMainAccountSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMainAccountSummaryRepository extends JpaRepository<UserMainAccountSummaryEntity, Integer> {
    UserMainAccountSummaryEntity findByUserName(String name);
}
