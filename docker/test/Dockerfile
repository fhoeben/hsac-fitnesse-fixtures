ARG  BASE_IMAGE=hsac/fitnesse-fixtures-test-jre11:base-latest
ARG  JRE_IMAGE=eclipse-temurin:11-jre

FROM ${BASE_IMAGE} as base

FROM ${JRE_IMAGE}
RUN mkdir -p /fitnesse/wiki/fixtures/nl/hsac/fitnesse

WORKDIR /fitnesse

RUN mkdir target
VOLUME /fitnesse/target

RUN mkdir wiki/FitNesseRoot
VOLUME /fitnesse/wiki/FitNesseRoot

ENV FITNESSE_OPTS -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true
COPY runTests.sh .
COPY rerunFailedTests.sh .
COPY htmlReportIndexGenerator.sh .

ENTRYPOINT ["/fitnesse/runTests.sh"]
CMD []

COPY --from=base /usr/src/test/wiki wiki/
