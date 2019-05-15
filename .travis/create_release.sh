#!/bin/bash
set -ev

VERSION=`date +%Y%m%d%H%M`
MVN_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
# Strip -SNAPSHOT if exists
MVN_VERSION=${MVN_VERSION/-SNAPSHOT/}

#overcome maven assemble issue: https://github.com/mojohaus/appassembler/issues/61
sed -i '/CYGWIN\*) cygwin=true/c\  CYGWIN*|MINGW*) cygwin=true ;;' target/appassembler/bin/utplsql

mkdir dist
mv target/appassembler utPLSQL-cli
# Remove Oracle libraries du to licensing problems
rm -f utPLSQL-cli/lib/ucp*.jar
rm -f utPLSQL-cli/lib/ojdbc8*.jar
rm -f utPLSQL-cli/lib/orai18n*.jar

zip -r -q dist/utPLSQL-cli-${TRAVIS_BRANCH}-${VERSION}.zip utPLSQL-cli
zip -r -q utPLSQL-cli.zip utPLSQL-cli
md5sum utPLSQL-cli.zip  --tag > utPLSQL-cli.zip.md5

cat > bintray.json <<EOF
{
  "package": {
    "name": "utPLSQL-cli-${TRAVIS_BRANCH}",
    "repo": "utPLSQL-cli",
    "subject": "utplsql",
    "website_url": "https://github.com/utPLSQL/utPLSQL-cli",
    "vcs_url": "https://github.com/utPLSQL/utPLSQL-cli.git",
    "licenses": [ "MIT" ]
  },
  "version": { "name": "${MVN_VERSION}" },
  "files": [ { "includePattern": "dist/(.*)", "uploadPattern": "\$1" } ],
  "publish": true
}
EOF
