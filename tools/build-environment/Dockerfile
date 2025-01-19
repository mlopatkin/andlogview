FROM eclipse-temurin:17.0.13_11-jdk-focal@sha256:dd2cf0a6705b6a3d09009e326daac98113c5b3e411f18233c92547e8b1596f83

ENV JDK17=/opt/java/openjdk
ENV JDK8=/opt/java/openjdk8

COPY --from=eclipse-temurin:8u432-b06-jdk-focal@sha256:38fd5ae69787f9712ff9e7a520d75532586bad8319f20c5e30e73acb22a57ac2 /opt/java/openjdk $JDK8

LABEL org.opencontainers.image.authors="Mikhail Lopatkin <me@mlopatkin.name>"
LABEL org.opencontainers.image.source="https://github.com/mlopatkin/andlogview"

# The safe.directory is to prevent Git warnings when running in the github action environment.
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      git \
      python3 \
      fakeroot \
    && \
    apt-get purge -y --auto-remove && \
    rm -rf /var/lib/apt/lists/* && \
    git config --system --add safe.directory /__w/andlogview/andlogview
