-- initial data
insert into USER_INFORMATION(user_name, update_on, updated_by)
values ('Bob1', '2021-08-01', 'admin');
insert into USER_INFORMATION(user_name, update_on, updated_by)
values ('Alice1', '2021-08-01', 'admin');

insert into USER_MAIN_ACCOUNT_SUMMARY(user_name, deposits_balance, outstanding_debt, creditor, update_on, updated_by)
values ('Bob1', 0, -40, 'Alice1', '2021-08-01', 'admin');
insert into USER_MAIN_ACCOUNT_SUMMARY(user_name, deposits_balance, outstanding_debt, creditor, update_on, updated_by)
values ('Alice1', 210, 40, '', '2021-08-01', 'admin');


insert into USER_INFORMATION(user_name, update_on, updated_by)
values ('Bob2', '2021-08-01', 'admin');
insert into USER_INFORMATION(user_name, update_on, updated_by)
values ('Alice2', '2021-08-01', 'admin');

insert into USER_MAIN_ACCOUNT_SUMMARY(user_name, deposits_balance, outstanding_debt, creditor, update_on, updated_by)
values ('Bob2', 0, -10, 'Alice2', '2021-08-01', 'admin');
insert into USER_MAIN_ACCOUNT_SUMMARY(user_name, deposits_balance, outstanding_debt, creditor, update_on, updated_by)
values ('Alice2', 210, 10, '', '2021-08-01', 'admin');