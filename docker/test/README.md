This docker image can run test using FitNesse and the baseline provided by [hsac-fitnesse-fixtures](https://github.com/fhoeben/hsac-fitnesse-fixtures).
Its source can be found on [GitHub](https://github.com/fhoeben/hsac-fitnesse-docker).

The tests to be run are expected to be supplied by a volume mounted to `/fitnesse/wiki/FitNesseRoot`. 
Test results will be written to `/fitnesse/target`, in surefire xml format (in `/fitnesse/target/failsafe-reports`) and in HTML (in `/fitnesse/target/fitnesse-results`).
By mounting a volume the host running the container can access these results after the test run is completed.
Or they can be copied from the stopped container to the host using `docker cp ${TEST_CONTAINER}:/fitnesse/target/. ${HOST_TARGET_DIR}` (this is easier permission-wise).

The image is preconfigured to start a FitNesse run immediately when started and you are expected to supply a suite to run using a system property value for 'fitnesseSuiteToRun'
(e.g. `-DfitnesseSuiteToRun=SampleTests.SlimTests.UtilityFixtures`). Other system properties arguments can also be supplied as arguments to 'docker run'.

Samples on how use the image can be found in [this image's GitHub repo's `buildTest.sh`](https://github.com/fhoeben/hsac-fitnesse-docker/blob/master/buildTest.sh) script, which after building the image
also run a container based on the newly created image (using wiki content from `src/main/wiki`). 

The image also contain a script to combine test results from multiple test runs in a single report: `htmlReportIndexGenerator.sh`.
This script can be invoked using `docker run` by changing the entrypoint and mounting the fitnesse-results created by individual runs.
A sample of how to do this can be found in [this image's GitHub repo `combineReports.sh`](https://github.com/fhoeben/hsac-fitnesse-docker/blob/master/combineReports.sh). 

The image also support re-running of failed tests. This can be useful when tests are a bit unstable, so they can be retried before declaring failure. This can be enabled by
supplying the environment variabble `RE_RUN_FAILED` with value `true`. The HTML of the re-run will be generated in `/fitnesse/target/fitnesse-rerun-results`, the surefire xml
report in `/fitnesse/target/failsafe-reports` will be overridden.