<#macro createRelease tag prerelease="true">
        uses: softprops/action-gh-release@[=RELEASE_ACTION_VERSION]
        if: ${{ success() }}
        with:
          body_path: docs/releases/release_0.24.md # This is a temporary location
          draft: true
          prerelease: [=prerelease]
          tag_name: [=tag]
          generate_release_notes: false
          make_latest: false
</#macro>
<#macro publishArtifacts tag>
        uses: softprops/action-gh-release@[=RELEASE_ACTION_VERSION]
        if: ${{ success() }}
        with:
          files: build/distributions/*
          tag_name: [=tag]
          fail_on_unmatched_files: true
          generate_release_notes: false
          make_latest: false
</#macro>
<#macro publishRelease tag prerelease="true">
        uses: softprops/action-gh-release@[=RELEASE_ACTION_VERSION]
        if: ${{ success() }}
        with:
          draft: false
          prerelease: [=prerelease]
          tag_name: [=tag]
          generate_release_notes: false
          make_latest: false
</#macro>
