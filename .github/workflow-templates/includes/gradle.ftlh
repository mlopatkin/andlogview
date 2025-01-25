<#macro runGradle tasks>
        uses: gradle/gradle-build-action@[=GRADLE_BUILD_ACTION_VERSION]
        # Do not use Gradle Daemon because we only have a single invocation.
        with:
          arguments: |
            -Porg.gradle.java.installations.fromEnv=JDK8,JDK17
            -Porg.gradle.java.installations.auto-download=false
            -Porg.gradle.java.installations.auto-detect=false
            --no-daemon
            --stacktrace
            [=tasks]
</#macro>
