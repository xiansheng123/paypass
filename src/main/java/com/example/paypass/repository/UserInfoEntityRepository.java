package com.example.paypass.repository;

import com.example.paypass.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInfoEntityRepository extends JpaRepository<UserInfoEntity, Integer> {
    UserInfoEntity findByUserName(String name);

    List<UserInfoEntity> findAllByUserName(String name);
}