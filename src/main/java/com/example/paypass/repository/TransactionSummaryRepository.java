package com.example.paypass.repository;

import com.example.paypass.entity.TransactionSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionSummaryRepository extends JpaRepository<TransactionSummaryEntity, Integer> {
}
