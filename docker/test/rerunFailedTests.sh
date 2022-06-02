#!/usr/bin/env sh

if [ -f '/fitnesse/wiki/FitNesseRoot/ReRunLastFailures.wiki' ]; then
    echo "Rerunning failed tests"
    ./runTests.sh "$@" -DfitnesseSuiteToRun=ReRunLastFailures -DfitnesseResultsDir=target/fitnesse-rerun-results
else
    echo "No tests to re-run found."
    exit 1
fi
