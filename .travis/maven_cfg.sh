#!/bin/bash
set -ev
cd $(dirname $(readlink -f $0))

# Download wagon-http recommended by Oracle.
# On maven latest version this is not needed, but travis doesn't have it.
if [ ! -f $CACHE_DIR/wagon-http-2.8-shaded.jar ]; then
    curl -L -O "http://central.maven.org/maven2/org/apache/maven/wagon/wagon-http/2.8/wagon-http-2.8-shaded.jar"
    mv wagon-http-2.8-shaded.jar $CACHE_DIR/
    sudo cp $CACHE_DIR/wagon-http-2.8-shaded.jar $MAVEN_HOME/lib/ext/
else
    echo "Using cached wagon-http..."
    sudo cp $CACHE_DIR/wagon-http-2.8-shaded.jar $MAVEN_HOME/lib/ext/
fi

# Create the settings file with oracle server config.
# If file already exists, Oracle dependencies were cached on previous build.
if [ ! -f $MAVEN_CFG/.cached ]; then
    cp settings.xml $MAVEN_CFG/settings.xml
    touch $MAVEN_CFG/.cached
else
    echo "Using cached maven settings..."
fi

# The Java API is not available on a public repository yet, we need to download and install it locally.
# Always downloading the latest development version.
git clone https://github.com/utPLSQL/utPLSQL-java-api.git
cd utPLSQL-java-api
mvn package install -DskipTests
