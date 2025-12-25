# When changing the version here, update the JDK version in .github/versions.properties.
# This is the version to bundle with Linux distribution, and it is good to have it consistent across platforms.
FROM eclipse-temurin:17.0.17_10-jdk@sha256:7bcd719950407380e7acd15b53b228673c82289a7d8c765cbdb0c89a75f17a12

ENV JDK17=/opt/java/openjdk
ENV JDK8=/opt/java/openjdk8
# Ensure reproducible DEB builds, 1980-01-31T23:00:00Z, something close to what Gradle uses as a default timestamp
ENV SOURCE_DATE_EPOCH=318207600

COPY --from=eclipse-temurin:8u472-b08-jdk@sha256:6c9af470aeafde76116d29f500e899454c42001b5d7f7385ae0aadebe851073e /opt/java/openjdk $JDK8

LABEL org.opencontainers.image.authors="Mikhail Lopatkin <me@mlopatkin.name>"
LABEL org.opencontainers.image.source="https://github.com/mlopatkin/andlogview"

# Install required tooling:
# - fakeroot - to build DEB packages
# - git - to update nightly tag and mirror to bitbucket
# - python3 - to run lint checks
# - rpm - to build RPM packages
# - wget - to install GH (removed afterwards)
# - gh (from GitHub repo) - to manage releases
# - unzip - for Gradle GH action
# The safe.directory is to prevent Git warnings when running in the github action environment.
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      fakeroot \
      git \
      python3 \
      rpm \
      wget \
    && \
    mkdir -p -m 755 /etc/apt/keyrings && \
    wget -nv -O/etc/apt/keyrings/githubcli-archive-keyring.gpg https://cli.github.com/packages/githubcli-archive-keyring.gpg && \
    chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" > /etc/apt/sources.list.d/github-cli.list && \
    apt-get update -o Dir::Etc::sourcelist=/etc/apt/sources.list.d/github-cli.list && \
    apt-get install gh -y && \
    apt-get purge -y --auto-remove wget && \
    rm -rf /var/lib/apt/lists/* && \
    git config --system --add safe.directory /__w/andlogview/andlogview
