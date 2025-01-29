#!/bin/sh

#
# Copyright 2025 the Andlogview authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e
#!/bin/sh

if [ $# -ne 2 ]; then
    echo "Usage: $0 <image-directory> <basename>"
    exit 1
fi

IMAGE_DIR=$1
BASENAME=$2
ICONSET="/tmp/${BASENAME}.iconset"

# Validate the directory
if [ ! -d "$IMAGE_DIR" ]; then
    echo "Error: Directory '$IMAGE_DIR' does not exist."
    exit 1
fi

# Clean up any existing temporary iconset directory
rm -rf "$ICONSET"
mkdir -p "$ICONSET"

# Define standard icon sizes and copy files
for SIZE in 16 32 128 256 512; do
    if [ -f "${IMAGE_DIR}/${BASENAME}.${SIZE}.png" ]; then
        cp "${IMAGE_DIR}/${BASENAME}.${SIZE}.png" "${ICONSET}/icon_${SIZE}x${SIZE}.png"
    fi

    # macOS requires @2x versions for high-resolution displays
    DOUBLE_SIZE=$((SIZE * 2))
    if [ -f "${IMAGE_DIR}/${BASENAME}.${DOUBLE_SIZE}.png" ]; then
        cp "${IMAGE_DIR}/${BASENAME}.${DOUBLE_SIZE}.png" "${ICONSET}/icon_${SIZE}x${SIZE}@2x.png"
    fi
done

# Convert to .icns format
iconutil -c icns "$ICONSET" -o "${IMAGE_DIR}/${BASENAME}.icns"

# Cleanup
rm -rf "$ICONSET"

echo "Created ${BASENAME}.icns"
