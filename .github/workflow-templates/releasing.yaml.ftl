<#import "gradle.ftlh" as gradle>
<#import "releases.ftlh" as releases>
name: Run releasing action (snapshot or release)
on:
  push:
    branches:
      - master
      - release/*
    tags-ignore:
      - "*-snapshot"
    paths-ignore:
      - 'docs/site/**'

env:
  PYTHON_BINARY: python3
jobs:
  build-snapshot:
    runs-on: ubuntu-latest
    container: ghcr.io/mlopatkin/andlogview-build-environment@[=ANDLOGVIEW_ENV_VERSION]
    # Only build snapshots on master
    if: github.ref == 'refs/heads/master'
    steps:
      - uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]
      - <@gradle.setupGradle />
      - name: Build and publish snapshot
        <@gradle.runGradle "check installers bitbucketUpload"/>
        env:
          BITBUCKET_PASSWORD: ${{ secrets.BITBUCKET_PASSWORD }}
          BITBUCKET_USER: mlopatkin
      - name: Publish artifacts
        uses: ./.github/actions/publish-gradle-outputs
      - name: Update Snapshot Tag
        if: ${{ success() }}
        run: |
          git tag --force "latest-snapshot" $GITHUB_SHA && \
          git push --force origin "latest-snapshot"
      - name: Remove old release
        if: ${{ success() }}
        env:
          GH_TOKEN: ${{ github.token }}
        shell: bash
        run: |
          tools/remove-release.sh "latest-snapshot" "$GITHUB_REPOSITORY"
      - name: Create new release
        <@releases.createRelease "latest-snapshot"/>
      - name: Publish Linux and cross-platform installers
        <@releases.publishArtifacts "latest-snapshot"/>

  build-snapshot-win:
    runs-on: windows-latest
    needs:
      - build-snapshot  # We need the release to be prepared first.
    # Only build snapshots on master
    if: github.ref == 'refs/heads/master'
    steps:
      - name: Checkout Repository
        uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]
      - name: Set up Eclipse Temurin JDK 17
        uses: actions/setup-java@[=SETUP_JAVA_ACTION_VERSION]
        with:
          distribution: 'temurin'
          java-version: '17.0.13'
      - <@gradle.setupGradle />
      - name: Build Windows Installers
        <@gradle.runGradle "windowsInstallers" ".\\gradlew.bat"/>
      - name: Add Windows Installers to release
        <@releases.publishArtifacts "latest-snapshot"/>

  publish-snapshot-release:
    runs-on: ubuntu-latest
    needs:
      - build-snapshot  # We need all the artifacts to be published
      - build-snapshot-win
    # Only build snapshots on master
    if: github.ref == 'refs/heads/master'
    steps:
      - name: Publish draft release as pre-release
        <@releases.publishRelease "latest-snapshot"/>

  build-release:
    runs-on: ubuntu-latest
    container: ghcr.io/mlopatkin/andlogview-build-environment@[=ANDLOGVIEW_ENV_VERSION]
    # Only build releases out of tags
    if: github.ref_type == 'tag' && !endsWith(github.ref, '-snapshot')
    steps:
      - uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]
      - <@gradle.setupGradle />
      - name: Build and publish release
        <@gradle.runGradle "check installers bitbucketUpload"/>
        env:
          BITBUCKET_PASSWORD: ${{ secrets.BITBUCKET_PASSWORD }}
          BITBUCKET_USER: mlopatkin
          LOGVIEW_SNAPSHOT_BUILD: false
      - name: Publish artifacts
        uses: ./.github/actions/publish-gradle-outputs

  mirror-to-bitbucket:
    runs-on: ubuntu-latest
    # build-snapshot publishes a tag that we want to sync with bitbucket as well, so wait for it to complete
    needs:
      - build-snapshot
    if: ${{ always() }}
    steps:
      - uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]
      - name: setup git for bitbucket
        run: tools/bitbucket-mirror/setup-bitbucket.sh
        shell: bash
        env:
          BITBUCKET_SSH_PRIVATE_KEY: ${{ secrets.BITBUCKET_SSH_KEY }}
          BITBUCKET_HOST_FINGERPRINT: ${{ secrets.BITBUCKET_HOST_FINGERPRINT }}
      - name: clone origin as mirror and push to bitbucket
        run: tools/bitbucket-mirror/clone-and-push.sh
        shell: bash
        env:
          MIRROR_REPO_PATH: ${{ runner.temp }}/origin
      - name: cleanup config
        run: tools/bitbucket-mirror/cleanup.sh
        shell: bash
        if: ${{ always() }}
