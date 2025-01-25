<#import "gradle.ftlh" as gradle>
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
      - <@gradle.setupGradle />
      - name: Run unit tests
        <@gradle.runGradle "--continue check shadowDistZip"/>
      - name: Publish artifacts
        uses: ./.github/actions/publish-gradle-outputs
  prevent-fixup-commit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]
      - name: Prevent fixup commits
        uses: 13rac1/block-fixup-merge-action@[=BLOCK_FIXUP_MERGE_ACTION_VERSION]
