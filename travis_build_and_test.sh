#!/bin/bash

set -e

if [ "${TRAVIS_PULL_REQUEST}" == "false" -a "${TRAVIS_BRANCH}" == "master" -a "${TEST_TYPE}" == "acceptance-test" ]; then
  echo -e "Create standalone.zip\n"
  mvn compile dependency:copy-dependencies assembly:single
fi

if [ "${TEST_TYPE}" == "acceptance-test" ]; then
  echo -e "Run acceptance test using PhantomJs on Travis server\n"
  mvn -DseleniumDriverClass=org.openqa.selenium.phantomjs.PhantomJSDriver test
elif [ "${TEST_TYPE}" == "examples" -a "${TRAVIS_PULL_REQUEST}" == "false" -a "${TRAVIS_BRANCH}" == "master" ]; then
  echo -e "Run examples via SauceLabs\n"
  mvn -DfitnesseSuiteToRun=HsacExamples -DseleniumGridUrl=http://${SAUCE_USERNAME}:${SAUCE_ACCESS_KEY}@ondemand.saucelabs.com:80/wd/hub -DseleniumCapabilities="${CAPA},name:Travis ${TRAVIS_BUILD_NUMBER}-examples,build:$TRAVIS_BUILD_NUMBER" test-compile failsafe:integration-test
fi
