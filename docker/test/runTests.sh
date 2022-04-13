#!/usr/bin/env sh

FITNESSE_CP="wiki/fixtures:wiki/fixtures/*"

CMD="java -cp ${FITNESSE_CP} ${FITNESSE_OPTS} $* nl.hsac.fitnesse.junit.JUnitConsoleRunner nl.hsac.fitnesse.HsacFitNesseSuiteStarter"
echo "${CMD}"
if [ "${RE_RUN_FAILED}" = "true" ]; then
    echo "An attempt will be made to rerun failed tests, should they occur."
fi

${CMD}

retVal=$?
if [ ${retVal} -ne 0 ] && [ "${RE_RUN_FAILED}" = "true" ]; then
    export RE_RUN_FAILED=false
    ./rerunFailedTests.sh "$@"
    retVal=$?
fi
exit ${retVal}
