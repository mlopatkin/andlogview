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
      - uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]
      - name: Run lint checks
        run: bash tools/hooks/lint-all.sh
      - name: Run unit tests
        uses: gradle/gradle-build-action@[=GRADLE_BUILD_ACTION_VERSION]
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
      - uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]
      - name: Prevent fixup commits
        uses: 13rac1/block-fixup-merge-action@[=BLOCK_FIXUP_MERGE_ACTION_VERSION]
