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
- Updated dependencies and shell scripts in docker container to remove vulnerabilities.

New in 5.2.5
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
