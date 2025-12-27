#!/bin/bash
set -euo pipefail

# Script to test the build in the same Docker container used by GitHub Actions
# This ensures consistency between local testing and CI/CD environment

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
IMAGE_NAME="andlogview-build-env"
CONTAINER_WORKSPACE="/__w/andlogview/andlogview"
GRADLE_CACHE_DIR="/tmp/andlogview-gradle-cache"

# Create Gradle cache directory if it doesn't exist
mkdir -p "$GRADLE_CACHE_DIR"

# Create gradle.properties with Java installation settings if it doesn't exist
GRADLE_PROPERTIES="$GRADLE_CACHE_DIR/gradle.properties"
if [ ! -f "$GRADLE_PROPERTIES" ]; then
  cat > "$GRADLE_PROPERTIES" <<'EOF'
org.gradle.java.installations.fromEnv=JDK17,JDK25
org.gradle.java.installations.auto-download=false
org.gradle.java.installations.auto-detect=false
EOF
  echo "Created $GRADLE_PROPERTIES with Java installation settings"
fi

echo "Building Docker image from tools/build-environment/Dockerfile..."
sudo docker build -t "$IMAGE_NAME" "$SCRIPT_DIR"

# Determine command to run
if [ $# -eq 0 ]; then
  COMMAND="./gradlew check installers"
else
  COMMAND="$*"
fi

echo ""
echo "Running command in Docker container: $COMMAND"
echo "Project root: $PROJECT_ROOT"
echo "Gradle cache: $GRADLE_CACHE_DIR"
echo ""

# Run the container with:
# - Current user's UID/GID to avoid permission issues
# - Working directory mounted to GitHub Actions path (already marked as safe in container)
# - Gradle cache mounted from host to persist dependencies between runs
# - HOME set to /tmp to avoid permission issues
# - GRADLE_USER_HOME set to mounted cache directory
sudo docker run --rm \
  --user "$(id -u):$(id -g)" \
  --volume "$PROJECT_ROOT:$CONTAINER_WORKSPACE" \
  --volume "$GRADLE_CACHE_DIR:/gradle-cache" \
  --workdir "$CONTAINER_WORKSPACE" \
  --env HOME=/tmp \
  --env GRADLE_USER_HOME=/gradle-cache \
  "$IMAGE_NAME" \
  bash -c "$COMMAND"

echo ""
echo "Build completed successfully!"
