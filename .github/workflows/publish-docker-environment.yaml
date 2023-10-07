name: Build and publish build environment docker image
on: workflow_dispatch

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: mlopatkin/andlogview-build-environment

jobs:
  build-and-push-image:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Log in to the Container registry
        # v3.0
        uses: docker/login-action@343f7c4344506bcbf9b4de18042ae17996df046d
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and push Docker image
        # v5.0
        uses: docker/build-push-action@0565240e2d4ab88bba5387d719585280857ece09
        with:
          context: tools/build-environment
          tags: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
          push: true
