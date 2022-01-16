#!/bin/bash
set -ev
cd $(dirname $(readlink -f $0))

PROJECT_FILE="utPLSQL-demo-project"
git clone -b develop --single-branch https://github.com/utPLSQL/utPLSQL-demo-project.git

cat > demo_project.sh.tmp <<EOF
sqlplus -S -L sys/oracle@//127.0.0.1:1521/xe AS SYSDBA <<SQL
create user ${DB_USER} identified by ${DB_PASS} quota unlimited on USERS default tablespace USERS;
grant create session, create procedure, create type, create table, create sequence, create view to ${DB_USER};
grant select any dictionary to ${DB_USER};
exit
SQL

cd ${PROJECT_FILE}
sqlplus -S -L ${DB_USER}/${DB_PASS}@//127.0.0.1:1521/xe <<SQL
whenever sqlerror exit failure rollback
whenever oserror  exit failure rollback

@source/award_bonus/employees_test.sql
@source/award_bonus/award_bonus.prc

@source/between_string/betwnstr.fnc

@source/remove_rooms_by_name/rooms.sql
@source/remove_rooms_by_name/remove_rooms_by_name.prc

@test/award_bonus/test_award_bonus.pks
@test/award_bonus/test_award_bonus.pkb

@test/between_string/test_betwnstr.pks
@test/between_string/test_betwnstr.pkb

@test/remove_rooms_by_name/test_remove_rooms_by_name.pks
@test/remove_rooms_by_name/test_remove_rooms_by_name.pkb

exit
SQL
EOF

docker cp ./$PROJECT_FILE $ORACLE_VERSION:/$PROJECT_FILE
docker cp ./demo_project.sh.tmp $ORACLE_VERSION:/demo_project.sh
docker exec $ORACLE_VERSION bash demo_project.sh
