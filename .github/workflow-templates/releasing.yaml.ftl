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
  <@releases.releasePipeline "snapshot" "nightly">
    # Only build snapshots on master
    if: github.ref == 'refs/heads/master'
  </@releases.releasePipeline>

  <@releases.releasePipeline "tag" "${{ github.ref_name }}">
    # Only build releases out of tags
    if: github.ref_type == 'tag' && !endsWith(github.ref, 'nightly')
  </@releases.releasePipeline>

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
