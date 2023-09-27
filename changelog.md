Release ${VERSION} of the project contains FitNesse ${FITNESSE_VERSION} and Selenium ${SELENIUM_VERSION}.

To get started by running some sample tests (if you have a Java runtime installed): just download the standalone.zip, extract it and run it (using '`java -jar fitnesse-standalone.jar -p 9090`', or on Windows just double clicking the jar to run the application on port `80` instead of `9090`) from the directory where the 'standalone.zip' was extracted and open a browser to `http://localhost:9090/HsacExamples`.

> Please note: please extract the zip to a location without spaces in its own name or in the names of its parents.

How to create your own test project using this FitNesse setup, see https://github.com/fhoeben/hsac-fitnesse-fixtures#to-create-your-own-test-project and https://github.com/fhoeben/hsac-fitnesse-fixtures/wiki/Installation-Guide

To incorporate the fixtures in an existing FitNesse project you can also get it from Maven Central

```
        <dependency>
            <groupId>nl.hsac</groupId>
            <artifactId>hsac-fitnesse-fixtures</artifactId>
            <version>${VERSION}</version>
        </dependency>
```

An example of Maven based project using this baseline can be found at: https://github.com/fhoeben/sample-fitnesse-project.

When upgrading from an older version, ensure all Selenium webdrivers are stopped and delete the 'wiki/plugins' and 'wiki/webdrivers' folders to remove old versions of the packaged plugins/webdrivers. When using the sample project: follow the instructions at https://github.com/fhoeben/sample-fitnesse-project#upgrading.

New in ${VERSION}
- The project no longer includes the Selenium webdriver for Internet Explorer
- EdgeDriver 117.0.2045.40
- ChromeDriver 117.0.5938.92

New in 5.2.44
- Selenium 4.12.0

New in 5.2.43
- ChromeDriver 116.0.5845.96
- EdgeDriver 116.0.1938.62

New in 5.2.42
- Actually included 64bit Windows Chromedriver

New in 5.2.41
- Update Edgedriver 115.0.1901.188
- Update Chromedriver 115.0.5790.170
- Switch to 64bit chromedriver
- Selenium 4.11.0
- SnakeYAML 2.1

New in 5.2.40
- Update Edgedriver 114.0.1823.18
- Update Chromedriver 114.0.5735.90
- Selenium 4.10.0

New in 5.2.39
- Remove workaround remote-allow-origins
- Update Edgedriver 113.0.1774.9
- Update Chromedriver 113.0.5672.24

New in 5.2.36
- Selenium 4.9.0
- JsonPath 2.8.0
- Edgedriver 112.0.1722.39
- GeckoDriver 0.33.0
- IEDriver 4.8.1

New in 5.2.35
- Selenium 4.8.3
- ChromeDriver 112.0.5615.49
- Edgedriver 112.0.1722.34

New in 5.2.34
- Fix options passed to chrome in chrome based docker images

New in 5.2.33
- ChromeDriver 111.0.5563.64
- Edgedriver 111.0.1661.41

New in 5.2.32
- ChromeDriver 110.0.5481.77
- Edgedriver 110.0.1587.41
- Update snakeyaml to 2.0, #475

New in 5.2.31
- Selenium 4.8.1
- Fix Chrome error "invalid argument: uniqueContextId not found" #471

New in 5.2.30
- Selenium 4.8.0
- Wrap devtools driver to Augmenter for RemoteWebDriver capability (#468)

New in 5.2.29
- Minor dependency updates

New in 5.2.28
- Praegus toolchain plugin 2.0.16

New in 5.2.27
- Fix: Chromedriver for Windows download

New in 5.2.26
- EdgeDriver 109.0.1518.55
- ChromeDriver 109.0.5414.74

New in 5.2.25
- Updated docker images to use base images that are also available on `arm64` platform

New in 5.2.24
- FitNesse 20221219
- Selenium 4.7.2
- hsac-fitnesse-plugin 1.32.10
- Other dependency updates

New in 5.2.23
  Fix acceptance test: HsacAcceptanceTests.SlimTests.BrowserTest.ClickByLabelOrButton
- Fix GitHub 'other' workflow to actually fail on test errors
- Replace Selenium deprecated `UNEXPECTED_ALERT_BEHAVIOUR` by `UNHANDLED_PROMPT_BEHAVIOUR`

New in 5.2.22
- Updated GitHub release process

New in 5.2.21
- Selenium 4.7.1

New in 5.2.20
- FitNesse 20221102
- EdgeDriver 108.0.1462.42
- ChromeDriver 108.0.5359.71

New in 5.2.19
- EdgeDriver 107.0.1418.26
- ChromeDriver 107.0.5304.62
- GeckoDriver 0.32.0

New in 5.2.18
- ChromeDriver 106.0.5249.61
- EdgeDriver 106.0.1370.34

New in 5.2.15
- Chromedriver 105.0.5195.52
- EdgeDriver 105.0.1343.25

New in 5.2.13
- FitNesse 20220815

New in 5.2.12
- ChromeDriver 104.0.5112.79
- EdgeDriver 104.0.1293.47

New in 5.2.11
- ChromeDriver 103.0.5060.53
- EdgeDriver 103.0.1264.37
  
New in 5.2.10
- EdgeDriver 102.0.1245.30
- update webdriverextensions-maven-plugin

New in 5.2.9
- ChromeDriver 102.0.5005.61

New in 5.2.8
- Fix: Chromedriver proxy configuration [#414](https://github.com/fhoeben/hsac-fitnesse-fixtures/pull/414)

New in 5.2.7
- Updated dependencies and shell scripts in docker container to remove vulnerabilities.
- Geckodriver 0.31.0
- EdgeDriver 101.0.1210.32
- ChromeDriver 101.0.4951.41
- IEDriver 4.0.0

New in 5.2.6
- Selenium 4.1.3

New in 5.2.5
- Edge WebDriver 100.0.1185.29

New in 5.2.4
- Nothing. Various optimizations to release GitHub actions.

New in 5.2.3
- Nothing. Fix to release pipeline to ensure packages are attached to the release page on GitHub.

- New in 5.2.2
- Chromedriver 100.0.4896.60

New in 5.2.1
- Nothing. This was a test release of a new automation step to ensure the Git tag would contain the correct version number

New in 5.2.0
- Update to FitNesse 20220319
- Update to hsac-fitnesse-plugin 1.32.2

New in 5.1.2
- Generate release description using `changelog.md`.  

New in 5.1.0
- Edgedriver 99.0.1150.30
- Chromedriver 99.0.4844.51

New in 5.0.0
- First release using Selenium 4 (#400), including support for shadow DOM
- Chromedriver and Geckodriver for Apple M1 chips included
- Chromedriver 98.0.4758.80
- Edgedriver 98.0.1108.43

New in 4.32.0
- Chromedriver 98.0.4758.48
- Edgedriver 97.0.1072.76

New in 4.31.0
- FitNesse 20211030
- Chromedriver 97.0.4692.71
- Edgedriver 96.0.1054.62
