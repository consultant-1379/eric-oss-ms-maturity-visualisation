#!/bin/sh
#
# COPYRIGHT Ericsson 2021
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

pwd
ls
echo "delete eric-oss-ms-maturity-visualisation folder to clone freshly"
rm -rf eric-oss-ms-maturity-visualisation
echo " cloning eric-oss-ms-maturity-visualisation"
git clone ssh://ossadmin@gerrit-gamma.gic.ericsson.se:29418/OSS/com.ericsson.oss.ci/eric-oss-ms-maturity-visualisation && scp -p -P 29418 ossadmin@gerrit-gamma.gic.ericsson.se:hooks/commit-msg eric-oss-ms-maturity-visualisation/.git/hooks/
if [ $? == 0 ]; then
  echo "files cloned"
else
  exit 1
fi
echo "checking current working directory"
pwd
echo " files which are in current working directory"
ls -la
echo "entering eric-oss-ms-maturity-visualisation inside the current working directory"
cd eric-oss-ms-maturity-visualisation
pwd
echo "files which are in eric-oss-ms-maturity-visualisation"
ls -la
git checkout master
git clone ssh://ossadmin@gerrit-gamma.gic.ericsson.se:29418/OSS/com.ericsson.oss.ci/oss-common-ci-utils && scp -p -P 29418 ossadmin@gerrit-gamma.gic.ericsson.se:hooks/commit-msg oss-common-ci-utils/.git/hooks/
echo " cloning oss-common-ci-utils"
cd oss-common-ci-utils
git checkout dVersion-2.0.0-hybrid
ls -la
cd ..
