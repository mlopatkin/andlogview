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

if [ -z "$BITBUCKET_SSH_PRIVATE_KEY" ]
then
  echo "Please set up base64-encoded BITBUCKET_SSH_PRIVATE_KEY. Exiting."
  exit 1
fi

if [ ! -d $SSH_DIR ]
then
  mkdir -p $SSH_DIR
  chmod 700 $SSH_DIR
fi

echo $BITBUCKET_SSH_PRIVATE_KEY | base64 -d > $BITBUCKET_SSH_KEYFILE
chmod 600 $BITBUCKET_SSH_KEYFILE

if [ -e $SSH_CONFIG ]
then
  cp $SSH_CONFIG $SSH_CONFIG_BACKUP
fi

cat << EOF >> $SSH_CONFIG
Host bitbucket
  HostName bitbucket.org
  IdentityFile $BITBUCKET_SSH_KEYFILE
  IdentitiesOnly yes
EOF

git remote add bitbucket $BITBUCKET_ORIGIN_SSH_URL
