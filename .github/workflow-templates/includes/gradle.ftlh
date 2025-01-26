<#macro runGradle tasks gradleCmd="./gradlew">
        run: >
            [=gradleCmd]
            -Porg.gradle.java.installations.fromEnv=JDK8,JDK17
            -Porg.gradle.java.installations.auto-download=false
            -Porg.gradle.java.installations.auto-detect=false
            --no-daemon
            --stacktrace
            [=tasks]
</#macro>
<#macro setupGradle>name: Setup Gradle
        uses: gradle/actions/setup-gradle@[=SETUP_GRADLE_ACTION_VERSION]</#macro>
