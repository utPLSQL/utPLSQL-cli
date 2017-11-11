#!/bin/bash
set -ev

sqlplus -S -L sys/oracle@//127.0.0.1:1521/xe AS SYSDBA <<EOF
create user api identified by api
quota unlimited on USERS
default tablespace USERS;
grant create session,
      create procedure,
	  create type,
	  create table,
	  create sequence,
	  create view
to api;
grant select any dictionary to api;
exit;
EOF
