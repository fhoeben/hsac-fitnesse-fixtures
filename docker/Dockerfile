ARG  MAVEN_VERSION=3.6-amazoncorretto-8
FROM maven:${MAVEN_VERSION} as build
RUN mkdir -p /usr/src
WORKDIR /usr/src

COPY pom.xml .

RUN mvn compile

COPY src/ ./src

RUN mvn package
