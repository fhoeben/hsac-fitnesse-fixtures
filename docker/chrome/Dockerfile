ARG  TEST_IMAGE=hsac/fitnesse-fixtures-test-jre8:latest
ARG  SELENIUM_VERSION=latest
FROM ${TEST_IMAGE} as hsac-fixtures

FROM selenium/standalone-chrome:${SELENIUM_VERSION}
RUN sudo mkdir -p /fitnesse/target/selenium-log
RUN sudo chown -R seluser:seluser /fitnesse/

WORKDIR /fitnesse
COPY startGridAndRunTests.sh .

VOLUME /fitnesse/target
VOLUME /fitnesse/wiki/FitNesseRoot

ENV JAVA_OPTS -Xmx96m -Xms96m
ENV FITNESSE_OPTS -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true \
    -DseleniumJsonCapabilities="{'browserName':'chrome','chromeOptions':{'args':['--disable-dev-shm-usage']}}" \
    -DseleniumGridUrl=http://localhost:4444/wd/hub

ENTRYPOINT ["/fitnesse/startGridAndRunTests.sh"]
CMD []

COPY --from=hsac-fixtures --chown=seluser:seluser /fitnesse /fitnesse
