FROM eclipse-temurin:17.0.8.1_1-jdk-focal@sha256:f61d04de5527d488ca1acbf7f90a0d3a6650b3da9b7438e3112982286bbdfb2c

ENV JDK17=/opt/java/openjdk
ENV JDK8=/opt/java/openjdk8

COPY --from=eclipse-temurin:8u382-b05-jdk-focal@sha256:2a92a919e3853128a4c0217604e4248e172eac67db2cb9150053a164bc2f8ded /opt/java/openjdk $JDK8

LABEL org.opencontainers.image.authors="Mikhail Lopatkin <me@mlopatkin.name>"
LABEL org.opencontainers.image.source="https://github.com/mlopatkin/andlogview"

RUN apt-get update && \
    apt-get install -y --no-install-recommends git python3 && \
    apt-get purge -y --auto-remove && \
    rm -rf /var/lib/apt/lists/* && \
    git config --system --add safe.directory /__w/andlogview/andlogview
