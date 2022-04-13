#!/usr/bin/env sh

FITNESSE_CP="wiki/fixtures:wiki/fixtures/*:wiki/fitnesse-standalone.jar"
java -cp "${FITNESSE_CP}" nl.hsac.fitnesse.junit.reportmerge.HtmlReportIndexGenerator "$@"
