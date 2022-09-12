#!/bin/bash

#
# Copyright 2022 the Andlogview authors
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

. $SCRIPT_DIR/env.sh

if [ ! -v MIRROR_REPO_PATH ]
then
  echo "Please set up MIRROR_REPO_PATH! Exiting."
  exit 1
fi

git clone https://github.com/mlopatkin/andlogview.git --bare $MIRROR_REPO_PATH
git -C $MIRROR_REPO_PATH remote add bitbucket $BITBUCKET_ORIGIN_SSH_URL
git -C $MIRROR_REPO_PATH push bitbucket --mirror
