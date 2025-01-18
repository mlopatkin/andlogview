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

# Check for correct usage
if [ "$#" -lt 1 ] || [ "$#" -gt 2 ]; then
    echo "Usage: $0 <svg_file> [output_base_name]"
    exit 1
fi

# Get the SVG filename without extension
SVG_FILE="$1"
BASE_NAME="${2:-$(basename "$SVG_FILE" .svg)}"
DIR_NAME="$(dirname "$SVG_FILE")"

# Hardcoded sizes taken from Chromium's post-install
SIZES="16 24 32 48 64 128 256"

# Check if inkscape and optipng are available
INKSCAPE="`command -v inkscape 2> /dev/null || true`"
if [ ! -x "$INKSCAPE" ]; then
  echo "Error: Could not find inkscape" >&2
  exit 1
fi

OPTIPNG="`command -v optipng 2> /dev/null || true`"
if [ ! -x "$OPTIPNG" ]; then
  echo "Error: Could not find optipng" >&2
  exit 1
fi

for SIZE in $SIZES; do
    OUTPUT_FILE="${DIR_NAME}/${BASE_NAME}.${SIZE}.png"
    echo "Resizing $SVG_FILE to ${SIZE}x${SIZE} -> $OUTPUT_FILE"
    "$INKSCAPE" -w "$SIZE" -h "$SIZE" "$SVG_FILE" -e "$OUTPUT_FILE"

    # Optimize PNG file
    echo "Optimizing $OUTPUT_FILE"
    "$OPTIPNG" -o7 "$OUTPUT_FILE"
done
