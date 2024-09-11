FROM eclipse-temurin:17-jdk
LABEL maintainer="Maximilian Kratz <account@maxkratz.com>"

# Update package of base system
RUN apt-get update -q && \
    apt-get upgrade -yq

# Configure time zone
ENV TZ=Europe/Berlin
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Install java requirements
RUN apt-get install -yq \
    build-essential \
    zip \
    unzip \
    curl

# Install maven (newer version compared to apt)
ARG MAVEN_VERSION=3.9.6
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries
RUN mkdir -p /usr/share/maven /usr/share/maven/ref
RUN curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz
RUN tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1
RUN rm -f /tmp/apache-maven.tar.gz
RUN ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven

# Install python requirements
RUN apt-get install -yq \
    python3

# Change workdir to /data
WORKDIR /data