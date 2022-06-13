#!/bin/bash
set -evx
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

cd /${PROJECT_FILE}
sqlplus -S -L ${DB_USER}/${DB_PASS}@//127.0.0.1:1521/xe <<SQL
whenever sqlerror exit failure rollback
whenever oserror  exit failure rollback
@source/install.sql
exit
SQL

sqlplus -S -L ${DB_USER}/${DB_PASS}@//127.0.0.1:1521/xe <<SQL
whenever sqlerror exit failure rollback
whenever oserror  exit failure rollback
@test/install.sql
exit
SQL
EOF

docker cp ./${PROJECT_FILE} oracle:/${PROJECT_FILE}
docker cp ./demo_project.sh.tmp oracle:/demo_project.sh
docker exec oracle bash /demo_project.sh
