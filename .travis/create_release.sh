#!/bin/bash
set -ev

VERSION=`date +%Y%m%d%H%M`

mkdir dist
mv target/appassembler utPLSQL-cli
# Remove Oracle libraries du to licensing problems
rm utPLSQL-cli/lib/ojdbc8*
rm utPLSQL-cli/lib/orai18n*

zip -r -q dist/utPLSQL-cli-${TRAVIS_BRANCH}-${VERSION}.zip utPLSQL-cli
zip -r -q utPLSQL-cli.zip utPLSQL-cli

cat > bintray.json <<EOF
{
  "package": {
    "name": "utPLSQL-cli-${TRAVIS_BRANCH}",
    "repo": "utPLSQL-cli",
    "subject": "${BINTRAY_USER}",
    "website_url": "https://github.com/utPLSQL/utPLSQL-cli",
    "vcs_url": "https://github.com/utPLSQL/utPLSQL-cli.git",
    "licenses": [ "MIT" ]
  },
  "version": { "name": "${TRAVIS_BRANCH}" },
  "files": [ { "includePattern": "dist/(.*)", "uploadPattern": "\$1" } ],
  "publish": true
}
EOF
