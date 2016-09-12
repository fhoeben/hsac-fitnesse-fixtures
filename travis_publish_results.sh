#!/bin/bash
if [ "${TRAVIS_PULL_REQUEST}" == "false" -a "${TRAVIS_BRANCH}" == "master" ]; then
  echo -e "Starting to update gh-pages\n"

  #store current dir
  pushd .
  #copy data we're interested in to other place
  cp -R $1 ${HOME}/$2

  #go to home and setup git
  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis"

  #using token clone gh-pages branch
  git clone --quiet --branch=gh-pages --depth 1 https://${GIT_HUB_TOKEN}@github.com/fhoeben/hsac-fitnesse-fixtures.git  gh-pages > /dev/null

  #go into directory, remove files from previous build and copy data we're interested in to that directory
  cd gh-pages
  rm -r $2
  cp -Rf ${HOME}/$2 $2

  #add, commit and push files
  git add -A -f .
  git commit -m "Travis build ${TRAVIS_BUILD_NUMBER} $2"
  git pull
  git push -fq origin gh-pages > /dev/null

  #go back to original dir
  popd
  echo -e "Done magic with results\n"
fi