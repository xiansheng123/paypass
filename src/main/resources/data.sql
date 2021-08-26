-- initial data
insert into USER_INFORMATION(user_name, update_on, updated_by)
values ('Bob', '2021-08-01', 'admin');
insert into USER_INFORMATION(user_name, update_on, updated_by)
values ('Alice', '2021-08-01', 'admin');

insert into USER_MAIN_ACCOUNT_SUMMARY(user_name, deposits_balance, outstanding_debt, creditor, update_on, updated_by)
values ('Bob', 0, 0, '', '2021-08-01', 'admin');
insert into USER_MAIN_ACCOUNT_SUMMARY(user_name, deposits_balance, outstanding_debt, creditor, update_on, updated_by)
values ('Alice', 0, 0, '', '2021-08-01', 'admin');