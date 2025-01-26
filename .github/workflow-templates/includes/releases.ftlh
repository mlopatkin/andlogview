<#macro createRelease tag prerelease="true">
        if: ${{ success() }}
        run: >
          gh release create [=tag]
            --draft
            --latest=false
            --notes-file docs/releases/release_0.24.md
            --prerelease=[=prerelease]
            --title [=tag]
            --verify-tag
        shell: bash
        env:
          GH_TOKEN: ${{ github.token }}
</#macro>
<#macro publishArtifacts tag>
        if: ${{ success() }}
        shell: bash
        run: gh release upload [=tag] build/distributions/*
        env:
          GH_TOKEN: ${{ github.token }}
</#macro>
<#macro publishRelease tag prerelease="true">
        if: ${{ success() }}
        run: >
          gh release edit [=tag]
            --draft=false
            --latest=false
            --prerelease=[=prerelease]
            --verify-tag
        shell: bash
        env:
          GH_TOKEN: ${{ github.token }}
</#macro>
