#!/bin/bash
set -ev
cd $(dirname $(readlink -f $0))

# Download the specified version of utPLSQL.
if [ "$UTPLSQL_VERSION" == "develop" ]
then
    git clone -b develop --single-branch https://github.com/utPLSQL/utPLSQL.git
else
    curl -L -O "https://github.com/utPLSQL/utPLSQL/releases/download/$UTPLSQL_VERSION/$UTPLSQL_FILE.tar.gz"
    tar -xzf ${UTPLSQL_FILE}.tar.gz && rm ${UTPLSQL_FILE}.tar.gz
fi

chmod -R go+w ./${UTPLSQL_FILE}/{source,examples}
# Create a temporary install script.
cat > install.sh.tmp <<EOF
cd /${UTPLSQL_FILE}/source
sqlplus -S -L sys/oracle@//127.0.0.1:1521/xe AS SYSDBA @install_headless.sql ut3 ut3 users
EOF

# Copy utPLSQL files to the container and install it.
docker cp ./${UTPLSQL_FILE} oracle:/${UTPLSQL_FILE}
# docker cp ./$UTPLSQL_FILE $ORACLE_VERSION:/$UTPLSQL_FILE
docker cp ./install.sh.tmp oracle:/install.sh
docker cp ./create_api_user.sh oracle:/create_api_user.sh
# Remove temporary files.
# rm $UTPLSQL_FILE.tar.gz
rm -rf $UTPLSQL_FILE
rm install.sh.tmp

# Execute the utPLSQL installation inside the container.
docker exec oracle bash /install.sh
docker exec oracle bash /create_api_user.sh
