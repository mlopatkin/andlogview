name: Windows Build demo
on:
  workflow_dispatch:

jobs:
  build-snapshot-win:
    runs-on: windows-latest
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
        run: gradlew.bat windowsInstallers

      - name: Upload Build Artifacts
        uses: actions/upload-artifact@v4
        with:
          name: windows-installers
          path: build/distributions
