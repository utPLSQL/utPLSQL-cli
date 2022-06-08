#!/bin/bash
set -ev

# If docker credentials are not cached, do the login.
if [ ! -f $DOCKER_CFG/config.json ]; then
    docker login -u "$DOCKER_USER" -p "$DOCKER_PASSWORD"
else
    echo "Using docker login from cache..."
fi

# Pull the specified db version from docker hub.
docker pull $DOCKER_REPO:$ORACLE_VERSION
docker run -d --name $ORACLE_VERSION $DOCKER_OPTIONS -p 1521:1521 $DOCKER_REPO:$ORACLE_VERSION
docker logs -f $ORACLE_VERSION | grep -m 1 "DATABASE IS READY TO USE!" --line-buffered
