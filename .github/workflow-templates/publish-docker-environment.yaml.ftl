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
        uses: actions/checkout@[=CHECKOUT_ACTION_VERSION]

      - name: Log in to the Container registry
        uses: docker/login-action@[=DOCKER_LOGIN_ACTION_VERSION]
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and push Docker image
        uses: docker/build-push-action@[=DOCKER_BUILD_PUSH_ACTION_VERSION]
        with:
          context: tools/build-environment
          tags: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
          push: true
