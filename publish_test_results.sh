#!/bin/bash
if [ "${TRAVIS_PULL_REQUEST}" == "false" ]; then
  echo -e "Starting to update gh-pages\n"

  #copy data we're interested in to other place
  cp -R target/fitnesse-results ${HOME}/acceptance-test-results

  #go to home and setup git
  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis"

  #using token clone gh-pages branch
  git clone --quiet --branch=gh-pages https://${GIT_HUB_TOKEN}@github.com/fhoeben/hsac-fitnesse-fixtures.git  gh-pages > /dev/null

  #go into directory, remove files from previous build and copy data we're interested in to that directory
  cd gh-pages
  rm -r *
  cp -Rf ${HOME}/acceptance-test-results acceptance-test-results

  #add, commit and push files
  git add -A -f .
  git commit -m "Travis build ${TRAVIS_BUILD_NUMBER} pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null

  echo -e "Done magic with results\n"
fi