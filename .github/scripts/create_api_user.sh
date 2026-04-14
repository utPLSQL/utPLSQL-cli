#!/bin/bash
set -ev

echo Running command: sqlplus -S -L sys/oracle@${DB_URL} AS SYSDBA
sqlplus -S -L sys/oracle@${DB_URL} AS SYSDBA <<EOF
create user api identified by api
quota unlimited on USERS
default tablespace USERS;
grant create session, create procedure, create type, create table, create sequence, create view to api;
grant select any dictionary to api;
exit;
EOF
