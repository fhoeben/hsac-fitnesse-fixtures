ARG  TEST_IMAGE=hsac/fitnesse-fixtures-test-jre8:latest
ARG  SELENIUM_VERSION=latest
FROM ${TEST_IMAGE} as hsac-fixtures

FROM selenium/standalone-chrome:${SELENIUM_VERSION}
RUN sudo mv /etc/supervisor/conf.d/selenium.conf /etc/supervisor/conf.d/selenium.conf.bak && \
    sudo mkdir -p /fitnesse/target && \
    sudo mkdir -p /fitnesse/wiki/webdrivers && \
    sudo ln -s /usr/bin/chromedriver /fitnesse/wiki/webdrivers/chromedriver-linux-64bit && \
    sudo chown -R 1200:1201 /fitnesse/

WORKDIR /fitnesse
COPY startGridAndRunTests.sh .
COPY fitnesse-hsac.conf /etc/supervisor/conf.d/

VOLUME /fitnesse/target
VOLUME /fitnesse/wiki/FitNesseRoot

ENV FITNESSE_OPTS -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true \
    -DseleniumBrowser=chrome \
    -DseleniumJsonProfile="{'args':['--disable-dev-shm-usage']}"

ENTRYPOINT ["/fitnesse/startGridAndRunTests.sh"]
CMD []

COPY --from=hsac-fixtures --chown=1200:1201 /fitnesse /fitnesse
