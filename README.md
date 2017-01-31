# hsac-fitnesse-fixtures
[![Build Status](https://travis-ci.org/fhoeben/hsac-fitnesse-fixtures.svg?branch=master)](https://travis-ci.org/fhoeben/hsac-fitnesse-fixtures) [![Maven Central](https://img.shields.io/maven-central/v/nl.hsac/hsac-fitnesse-fixtures.svg?maxAge=86400)](https://mvnrepository.com/artifact/nl.hsac/hsac-fitnesse-fixtures)

This project assists in testing (SOAP) web services and web applications by providing an application to define and run tests. To this end it contains a baseline installation of FitNesse (an acceptance testing wiki framework) and some FitNesse fixture (base) classes.

The fixtures provided aim to assist in testing (SOAP) web services and web applications (using Selenium) minimizing the amount of (custom) Java code needed to define tests.

The baseline FitNesse installation offers the following features:
* Ability to easily create a standalone (no JDK or Maven required) FitNesse environment.
* Run FitNesse tests on a build server, reporting the results in both JUnit XML format and HTML.
* FitNesse installation for test/fixture developers containing:
    - the fixture base classes (and Selenium drivers for _Chrome_, _Internet Explorer_, _Edge_ and _PhantomJs_),
    - Maven classpath plugin (such that tests can use all dependencies from `pom.xml`),
    - HSAC's fitnesse-plugin to add additional Wiki features (random values, calculating relative dates,
      Slim scenarios without need to specify all parameters, Slim scripts that take a screenshot after each step),
    - easy fixture debugging,

The fastest way to get started: just download the 'standalone.zip' from the 
[Releases](https://github.com/fhoeben/hsac-fitnesse-fixtures/releases/latest), extract, run it (you'll just need a
Java runtime) and explore the example tests and maybe add a couple of your own.

## To create your own test project
When you want to use the project's baseline to create and maintain your own test suite, we recommend creating your own Maven project based on the project. This will allow you to run and maintain your test set, in version control, without the need to keep your own copies of dependencies (neither Selenium WebDrivers nor Java libraries).

To create a Maven project to run your own tests, based on this project's baseline (assuming you already have a working [Maven installation](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html):
 * Go to the directory below which you want to create the project.
 * Use the [archetype, `nl.hsac:fitnesse-project`,](https://github.com/fhoeben/fitnesse-project-archetype) to generate the project: 
   * On the commandline execute: `mvn archetype:generate -DarchetypeGroupId=nl.hsac -DarchetypeArtifactId=fitnesse-project` (or use your IDE's ability to create a project from an archetype).
   * Answer the prompts for 'groupId', 'artifactId' and 'package' for the project.
 * _Alternatively_ you could also manually copy and update a sample project: 
   * Clone or download the [sample project](https://github.com/fhoeben/sample-fitnesse-project) to a new directory.
   * Update the 'groupId' and 'artifactId' in the [`pom.xml`](https://github.com/fhoeben/sample-fitnesse-project/blob/master/pom.xml) downloaded as part of the sample to reflect your own organisation and project name.
   * Move the file for Java class [`FixtureDebugTest`](https://github.com/fhoeben/sample-fitnesse-project/blob/master/src/test/java/nl/hsac/fitnesse/sample_project/FixtureDebugTest.java) to a package/directory of your choice.
 * Start the wiki, as described in the generated project's [`README.md`](https://github.com/fhoeben/fitnesse-project-archetype/blob/master/src/main/resources/archetype-resources/README.md#running-locally).
 * Start writing tests (and custom fixtures if needed)...

## To create the standalone FitNesse installation:
Execute `mvn clean package -DskipTests`, the standalone installation is present in the wiki
directory and as `...-standalone.zip` file in the target directory. It can be distributed by just copying the wiki directory or by copying and extracting the zip file to a location without spaces in its own name, or in its parent's names).
This standalone installation can be started using `java -jar fitnesse-standalone.jar` from the wiki directory (or directory where the _standalone.zip_ was extracted).

A zip file containing released versions of this project can be downloaded from the [Releases](https://github.com/fhoeben/hsac-fitnesse-fixtures/releases) or [Maven Central](http://central.maven.org/maven2/nl/hsac/hsac-fitnesse-fixtures/).
A similar zip file containing the latest *snapshot* (i.e. not released but based on the most recent code) version is published as part of the automated build of this project at http://fhoeben.github.io/hsac-fitnesse-fixtures/hsac-fitnesse-fixtures-snapshot-standalone.zip.

## To run the tests on a build server:
Have the build server checkout the project and execute `mvn clean test-compile failsafe:integration-test`. Append `failsafe:verify` to the command if you want the build to fail in case of test failures.
The result in JUnit XML results can be found in: `target/failsafe-reports` (most build servers will pick these up automatically)
The HTML results can be found in: `target/fitnesse-results/index.html`

The FitNesse suite to run can be specified by changing the value of the `@Suite` annotation in `nl.hsac.fitnesse.fixture.FixtureDebugTest`, or (preferably) by adding a system property, called `fitnesseSuiteToRun`, specifying the suite to run to the build server's mvn execution.

The Selenium configuration (e.g. what browser on what platform) to use when testing websites can be overridden by using system properties (i.e. `seleniumGridUrl` and either `seleniumBrowser` or `seleniumCapabilities`).
This allows different configurations on the build server to test with different browsers, without requiring different Wiki content, but only requiring a different build configuration.

### Reports
Example reports for Windows using a Sauce Labs Selenium driver (http://fhoeben.github.io/hsac-fitnesse-fixtures/examples-results/) and Linux with PhantomJs (http://fhoeben.github.io/hsac-fitnesse-fixtures/acceptance-test-results/) are generated in the automated build process of this project.

## Fixture developer installation:
Import this project in your favorite Java IDE (with Maven support).

To start FitNesse: have the IDE execute `mvn compile exec:exec`. The port used by FitNesse can be controlled by changing the `fitnesse.port` property's value in pom.xml.
FitNesse will be available at `http://localhost:<fitnesse.port>/`, example usage of the symbols and fixtures can be seen in `http://localhost:<fitnesse.port>/HsacExamples`.

To debug a fixture used in a FitNesse page: change the `@Suite` annotation's value to contain page name in `nl.hsac.fitnesse.fixture.FixtureDebugTest`, then just debug this test.

## Documentation
More information about this project can be found on its [GitHub Wiki](https://github.com/fhoeben/hsac-fitnesse-fixtures/wiki)
