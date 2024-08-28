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

echo "MOVING UPDATED JOB.CSV FILE"
cd eric-oss-ms-maturity-visualisation
echo "CHECKING BRANCH"
/usr/bin/git branch -a
echo "changing branch"
/usr/bin/git checkout master
/usr/bin/git branch
echo "Checking git status"
/usr/bin/git status
echo "ADDING > COMMITING > PUSHING"
/usr/bin/git clean -fdx
/usr/bin/git add .
/usr/bin/git commit -m "AEAT-548 modified files to fetch and upload new applications into jobs.csv"
/usr/bin/git push ssh://gerrit-gamma.gic.ericsson.se:29418/OSS/com.ericsson.oss.ci/eric-oss-ms-maturity-visualisation HEAD:refs/for/master
