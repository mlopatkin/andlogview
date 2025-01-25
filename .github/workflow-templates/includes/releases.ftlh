<#macro createRelease tag>
        uses: ncipollo/release-action@[=RELEASE_ACTION_VERSION]
        if: ${{ success() }}
        with:
          allowUpdates: false
          bodyFile: docs/releases/release_0.24.md # This is a temporary location
          makeLatest: false
          draft: true
          removeArtifacts: true
          tag: [=tag]
          updateOnlyUnreleased: true
</#macro>
<#macro publishArtifacts tag>
        uses: ncipollo/release-action@[=RELEASE_ACTION_VERSION]
        if: ${{ success() }}
        with:
          allowUpdates: true
          artifactErrorsFailBuild: true
          artifacts: build/distributions/*
          omitBody: true
          omitName: true
          removeArtifacts: false
          replacesArtifacts: false
          tag: [=tag]
          updateOnlyUnreleased: true
</#macro>
<#macro publishRelease tag prerelease="true">
        uses: ncipollo/release-action@[=RELEASE_ACTION_VERSION]
        if: ${{ success() }}
        with:
          allowUpdates: true
          draft: false
          omitBody: true
          omitName: true
          prerelease: [=prerelease]
          removeArtifacts: false
          replacesArtifacts: false
          tag: [=tag]
          updateOnlyUnreleased: true
</#macro>
