#!/bin/bash

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

set -euo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <TAG> <REPO>"
    exit 1
fi

if [ -z "$GH_TOKEN" ]; then
    echo "Error: GH_TOKEN environment variable is not set."
    exit 1
fi

TAG="$1"
REPO="$2"

release_status=$(gh release view "$TAG" --repo "$REPO" 2>&1 >/dev/null)
exit_code=$?

if [ $status -eq 0 ]; then
    gh release delete "$TAG" --yes --repo "$REPO"
elif echo "$output" | grep -q "release not found"; then
    echo "Release for $TAG is not present, skipping deletion"
    exit 0
else
    echo "Command failed with unexpected error:"
    echo "$output"
    exit $status
fi
