#!/bin/bash

# stop script on non-zero exit code by command
set -e

if [ "${TRAVIS_PULL_REQUEST}" == "true" ]; then
  # GitLab does not run pipelines for PR on forks (yet), so we run on travis
  echo -e "Run acceptance test using Headless Chrome on Travis server\n"
  mvn -DseleniumBrowser=chrome "-DseleniumJsonProfile={'args':['headless', 'disable-gpu']}" test
fi
