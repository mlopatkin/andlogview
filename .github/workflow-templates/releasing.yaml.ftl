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
    container: ghcr.io/mlopatkin/andlogview-build-environment@sha256:5d34e961a07019b09599025c5de65f3e7d6a44b80072f92114e35bde212e41b8
    # Only build snapshots on master
    if: github.ref == 'refs/heads/master'
    steps:
      - uses: actions/checkout@v4
      - name: Build and publish snapshot
        # gradle-build-action 2.9.0
        uses: gradle/gradle-build-action@842c587ad8aa4c68eeba24c396e15af4c2e9f30a
        # Do not use Gradle Daemon because we only have a single invocation.
        with:
          arguments: |
            -Porg.gradle.java.installations.fromEnv=JDK8,JDK17
            -Porg.gradle.java.installations.auto-download=false
            -Porg.gradle.java.installations.auto-detect=false
            --no-daemon
            --stacktrace
            check installers bitbucketUpload
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
      - name: Clean up old release
        if: ${{ success() }}
        env:
          GH_TOKEN: ${{ github.token }}
        shell: bash
        run: |
          tools/remove-release.sh "latest-snapshot" "$GITHUB_REPOSITORY"
      - name: Publish Github release
        uses: ncipollo/release-action@cdcc88a9acf3ca41c16c37bb7d21b9ad48560d87 # v1.15.0
        if: ${{ success() }}
        with:
          allowUpdates: true
          artifactErrorsFailBuild: true
          artifacts: build/distributions/*
          bodyFile: docs/releases/release_0.24.md # This is a temporary location
          makeLatest: false
          prerelease: true
          removeArtifacts: true
          tag: latest-snapshot
          updateOnlyUnreleased: true

  build-snapshot-win:
    runs-on: windows-latest
    needs:
      - build-snapshot  # We need the release to be prepared first.
    # Only build snapshots on master
    if: github.ref == 'refs/heads/master'
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4
      - name: Set up Eclipse Temurin JDK 17
        uses: actions/setup-java@v4.6.0
        with:
          distribution: 'temurin'
          java-version: '17.0.13'
      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
      - name: Run Gradle Task 'windowsInstallers'
        run: .\gradlew.bat windowsInstallers --stacktrace
      - name: Add Windows Installers to release
        uses: ncipollo/release-action@cdcc88a9acf3ca41c16c37bb7d21b9ad48560d87 # v1.15.0
        if: ${{ success() }}
        with:
          allowUpdates: true
          artifactErrorsFailBuild: true
          artifacts: build/distributions/*
          omitBody: true
          omitName: true
          removeArtifacts: false
          replacesArtifacts: false
          tag: latest-snapshot
          updateOnlyUnreleased: true

  build-release:
    runs-on: ubuntu-latest
    container: ghcr.io/mlopatkin/andlogview-build-environment@sha256:5d34e961a07019b09599025c5de65f3e7d6a44b80072f92114e35bde212e41b8
    # Only build releases out of tags
    if: github.ref_type == 'tag' && !endsWith(github.ref, '-snapshot')
    steps:
      - uses: actions/checkout@v4
      - name: Build and publish release
        # gradle-build-action 2.9.0
        uses: gradle/gradle-build-action@842c587ad8aa4c68eeba24c396e15af4c2e9f30a
        # Do not use Gradle Daemon because we only have a single invocation.
        with:
          arguments: |
            -Porg.gradle.java.installations.fromEnv=JDK8,JDK17
            -Porg.gradle.java.installations.auto-download=false
            -Porg.gradle.java.installations.auto-detect=false
            --no-daemon
            --stacktrace
            check installers bitbucketUpload
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
      - uses: actions/checkout@v4
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
