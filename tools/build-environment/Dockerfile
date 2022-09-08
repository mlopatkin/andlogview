FROM eclipse-temurin:8u345-b01-jdk-focal@sha256:fe980330281ad522f9a1d5364af48fee94f3860f7617a2e62c8eb5483146b69d

LABEL org.opencontainers.image.authors="Mikhail Lopatkin <me@mlopatkin.name>"
LABEL org.opencontainers.image.source="https://github.com/mlopatkin/andlogview"

RUN apt-get update && \
    apt-get install -y --no-install-recommends git python3 && \
    apt-get purge -y --auto-remove && \
    rm -rf /var/lib/apt/lists/*
