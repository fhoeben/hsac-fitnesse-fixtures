ARG TEST_IMAGE=hsac/fitnesse-fixtures-test-jre8-with-pdf:latest
ARG TEST_CHROME_IMAGE=hsac/fitnesse-fixtures-test-jre8-chrome:latest

FROM ${TEST_IMAGE} as test

FROM ${TEST_CHROME_IMAGE}
COPY --from=test /fitnesse/wiki/fixtures /fitnesse/wiki/fixtures
