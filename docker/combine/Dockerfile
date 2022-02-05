ARG  BASE_IMAGE=hsac/fitnesse-fixtures-test-jre8:base-latest
ARG  GRAALVM_IMAGE=ghcr.io/graalvm/native-image:latest
ARG  BUSYBOX_IMAGE=busybox:latest

FROM ${BASE_IMAGE} as base

FROM ${GRAALVM_IMAGE} as graal
RUN mkdir -p /fitnesse/target

WORKDIR /fitnesse

COPY --from=base /usr/src/combine/target/hsac-html-report-generator.jar target/

RUN native-image -jar target/hsac-html-report-generator.jar --static

FROM ${BUSYBOX_IMAGE}
WORKDIR /fitnesse
VOLUME /fitnesse/target

ENTRYPOINT ["/fitnesse/hsac-html-report-generator"]
CMD []

COPY --from=graal /fitnesse/hsac-html-report-generator .
