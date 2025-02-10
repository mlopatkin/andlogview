<#macro createRelease tag prerelease="true">
        if: ${{ success() }}
        run: >
          gh release create "[=tag]"
          --draft
          --latest=false
          --notes-file docs/releases/release_0.24.md
          --prerelease=[=prerelease]
          --title "[=tag]"
          --verify-tag
        shell: bash
        env:
          GH_TOKEN: ${{ github.token }}
</#macro>
<#macro publishArtifacts tag>
        if: ${{ success() }}
        shell: bash
        run: gh release upload "[=tag]" build/distributions/*
        env:
          GH_TOKEN: ${{ github.token }}
</#macro>
<#macro publishRelease tag prerelease="true">
        if: ${{ success() }}
        run: >
          gh release edit "[=tag]"
          --draft=false
          --latest=false
          --prerelease=[=prerelease]
          --verify-tag
          --repo "$GITHUB_REPOSITORY"
        shell: bash
        env:
          GH_TOKEN: ${{ github.token }}
</#macro>
<#macro releasePipeline pipeline tag>
  <#local prerelease=(pipeline == "snapshot")?then("true", "false")/>
  build-[=pipeline]:
    runs-on: ubuntu-latest
    container: ghcr.io/mlopatkin/andlogview-build-environment@[=ANDLOGVIEW_ENV_VERSION]
    env:
      LOGVIEW_SNAPSHOT_BUILD: [=prerelease]
    <#nested/>
    steps:
      - uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]
      - <@gradle.setupGradle />
      - name: Run checks and build noJRE installer
        # This can benefit from the configuration cache, unlike the installer builds later
        <@gradle.runGradle "check noJreInstaller bitbucketUpload"/>
        env:
          BITBUCKET_PASSWORD: ${{ secrets.BITBUCKET_PASSWORD }}
          BITBUCKET_USER: mlopatkin
      - name: Build and publish [=pipeline]
        <@gradle.runGradle "linuxInstallers"/>
      - name: Publish artifacts
        uses: ./.github/actions/publish-gradle-outputs
      <#if pipeline == "snapshot">
      - name: Update Snapshot Tag
        if: ${{ success() }}
        run: |
          git tag --force "[=tag]" $GITHUB_SHA && \
          git push --force origin "[=tag]"
      - name: Remove old release
        if: ${{ success() }}
        env:
          GH_TOKEN: ${{ github.token }}
        shell: bash
        run: |
          tools/remove-release.sh "[=tag]" "$GITHUB_REPOSITORY"
      </#if>
      - name: Create new release
        <@releases.createRelease "[=tag]" "[=prerelease]"/>
      - name: Publish Linux and cross-platform installers
        <@releases.publishArtifacts "[=tag]"/>

  build-[=pipeline]-win:
    runs-on: windows-latest
    needs:
      - build-[=pipeline]  # We need the release to be prepared first.
    <#nested/>
    steps:
      - name: Checkout Repository
        uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]
        <#if pipeline == "snapshot">
        with:
          # Building the version number requires full history, but it is only used on Windows
          fetch-depth: 0
        </#if>
      - name: Set up Eclipse Temurin JDK 17
        uses: actions/setup-java@[=SETUP_JAVA_ACTION_VERSION]
        with:
          distribution: 'temurin'
          java-version: '17.0.13'
      - <@gradle.setupGradle />
      - name: Build Windows Installers
        shell: cmd
        <@gradle.runGradle "windowsInstallers" "gradlew.bat"/>
        env:
          LOGVIEW_SNAPSHOT_BUILD: [=prerelease]
      - name: Add Windows Installers to release
        <@releases.publishArtifacts "[=tag]"/>

  build-[=pipeline]-mac:
    runs-on: macos-latest
    needs:
      - build-[=pipeline]  # We need the release to be prepared first.
    <#nested/>
    steps:
      - name: Checkout Repository
        uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]
      - name: Set up Eclipse Temurin JDK 17
        uses: actions/setup-java@[=SETUP_JAVA_ACTION_VERSION]
        with:
          distribution: 'temurin'
          java-version: '17.0.13'
      - <@gradle.setupGradle />
      - name: Build macOS Installers
        shell: sh
        <@gradle.runGradle "macosInstallers" />
        env:
          LOGVIEW_SNAPSHOT_BUILD: [=prerelease]
      - name: Add macOS Installers to release
        <@releases.publishArtifacts "[=tag]"/>

  <#if pipeline == "snapshot">
  publish-[=pipeline]-release:
    runs-on: ubuntu-latest
    needs:
      - build-[=pipeline]  # We need all the artifacts to be published
      - build-[=pipeline]-win
      - build-[=pipeline]-mac
    <#nested/>
    steps:
      - name: Publish draft release as pre-release
        <@releases.publishRelease "[=tag]" "[=prerelease]"/>
  </#if>
</#macro>
