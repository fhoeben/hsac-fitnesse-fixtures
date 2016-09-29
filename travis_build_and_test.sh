#!/bin/bash

# stop script on non-zero exit code by command
set -e

if [ "${TEST_TYPE}" == "acceptance-test" ]; then
  if [ "${TRAVIS_PULL_REQUEST}" == "false" -a "${TRAVIS_BRANCH}" == "master" ]; then
    echo -e "Create standalone.zip\n"
    mvn package -DskipTests
  fi

  echo -e "Run acceptance test using PhantomJs on Travis server\n"
  mvn -DseleniumDriverClass=org.openqa.selenium.phantomjs.PhantomJSDriver test

  ./travis_publish_results.sh target/fitnesse-results acceptance-test-results
elif [ "${TEST_TYPE}" == "examples" -a "${TRAVIS_PULL_REQUEST}" == "false" -a "${TRAVIS_BRANCH}" == "master" ]; then
  echo -e "Run examples via SauceLabs\n"
  mvn -DfitnesseSuiteToRun=HsacExamples -DseleniumGridUrl=http://${SAUCE_USERNAME}:${SAUCE_ACCESS_KEY}@ondemand.saucelabs.com:80/wd/hub -DseleniumCapabilities="${CAPA},name:Travis ${TRAVIS_BUILD_NUMBER}-examples,build:$TRAVIS_BUILD_NUMBER" test-compile failsafe:integration-test

  ./travis_publish_results.sh target/fitnesse-results examples-results
fi
