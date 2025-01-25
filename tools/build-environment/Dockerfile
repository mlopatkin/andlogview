FROM eclipse-temurin:17.0.13_11-jdk-focal@sha256:dd2cf0a6705b6a3d09009e326daac98113c5b3e411f18233c92547e8b1596f83

ENV JDK17=/opt/java/openjdk
ENV JDK8=/opt/java/openjdk8

COPY --from=eclipse-temurin:8u432-b06-jdk-focal@sha256:38fd5ae69787f9712ff9e7a520d75532586bad8319f20c5e30e73acb22a57ac2 /opt/java/openjdk $JDK8

LABEL org.opencontainers.image.authors="Mikhail Lopatkin <me@mlopatkin.name>"
LABEL org.opencontainers.image.source="https://github.com/mlopatkin/andlogview"

# Install required tooling:
# - fakeroot - to build DEB packages
# - git - to update nightly tag and mirror to bitbucket
# - python3 - to run lint checks
# - rpm - to build RPM packages
# - wget - to install GH (removed afterwards)
# - gh (from GitHub repo) - to manage releases
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
