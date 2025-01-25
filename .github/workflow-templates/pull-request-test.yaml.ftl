name: PR Tests for Github
on:
  pull_request:
    branches:
      - master
      - release/*
jobs:
  run-precommit-tests:
    runs-on: ubuntu-latest
    container: ghcr.io/mlopatkin/andlogview-build-environment@[=ANDLOGVIEW_ENV_VERSION]
    env:
      PYTHON_BINARY: python3
    steps:
      - uses: actions/checkout@v4
      - name: Run lint checks
        run: bash tools/hooks/lint-all.sh
      - name: Run unit tests
        # gradle-build-action 2.9.0
        uses: gradle/gradle-build-action@842c587ad8aa4c68eeba24c396e15af4c2e9f30a
        with:
          # Do not use Gradle Daemon because we only have a single invocation.
          arguments: |
            -Porg.gradle.java.installations.fromEnv=JDK8,JDK17
            -Porg.gradle.java.installations.auto-download=false
            -Porg.gradle.java.installations.auto-detect=false
            --no-daemon
            --stacktrace
            --continue
            check
            shadowDistZip
      - name: Publish artifacts
        uses: ./.github/actions/publish-gradle-outputs
  prevent-fixup-commit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Prevent fixup commits
        # block-fixup-merge-action v2.0.0
        uses: 13rac1/block-fixup-merge-action@bd5504fb9ca0253e109d98eb86b7debc01970cdc
