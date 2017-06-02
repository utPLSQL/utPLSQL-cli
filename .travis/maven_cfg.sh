#!/bin/bash
set -ev
cd $(dirname $(readlink -f $0))

if [ -f $MAVEN_CFG/repository ]; then
    echo "Using cached maven dependencies..."
    exit 0
fi

if [ "$ORACLE_OTN_USER" == "" ] || [ "$ORACLE_OTN_PASSWORD" == "" ]; then
    echo "Oracle OTN username/password not specified."
    exit 1
fi

# Download wagon-http recommended by Oracle.
# On maven latest version this is not needed, but travis doesn't have it.
curl -L -O "http://central.maven.org/maven2/org/apache/maven/wagon/wagon-http/2.8/wagon-http-2.8-shaded.jar"
sudo mv wagon-http-2.8-shaded.jar $MAVEN_HOME/lib/ext/

# Create the settings file with oracle server config.
cp settings.xml $MAVEN_CFG/settings.xml

# The Java API is not available on a public repository yet, we need to download and install it locally.
git clone https://github.com/utPLSQL/utPLSQL-java-api.git
cd utPLSQL-java-api
mvn package install -DskipTests
