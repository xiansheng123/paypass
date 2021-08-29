DROP TABLE IF EXISTS USER_INFORMATION;
DROP TABLE IF EXISTS USER_MAIN_ACCOUNT_SUMMARY;
DROP TABLE IF EXISTS TRANSACTION_SUMMARY;

CREATE TABLE USER_INFORMATION
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_name  VARCHAR(100) not null,
    update_on  TIMESTAMP    not null,
    updated_by VARCHAR(100) not null
);

-- Main Account Info
CREATE TABLE USER_MAIN_ACCOUNT_SUMMARY
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    user_name        VARCHAR(100)   not null,
    deposits_balance DECIMAL(10, 2) not null,
    outstanding_debt DECIMAL(10, 2) not null,
    creditor           VARCHAR(100)   not null,
    update_on        TIMESTAMP      not null,
    updated_by       VARCHAR(100)   null
);

-- Transaction Summary
CREATE TABLE TRANSACTION_SUMMARY
(
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    from_user          VARCHAR(100) not null,
    to_user            VARCHAR(100) not null,
    action             VARCHAR(50)  not null,
    transaction_detail JSON         not null,
    update_on          TIMESTAMP    not null,
    updated_by         VARCHAR(100) null
);