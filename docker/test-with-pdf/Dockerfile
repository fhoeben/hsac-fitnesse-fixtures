ARG  BASE_IMAGE=hsac/fitnesse-fixtures-test-jre8:base-latest
ARG  TEST_IMAGE=hsac/fitnesse-fixtures-test-jre8:latest

FROM ${BASE_IMAGE} as base
RUN mvn compile -P withPdf

FROM ${TEST_IMAGE}
COPY --from=base /usr/src/test/wiki/fixtures wiki/fixtures
