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

rm -f $BITBUCKET_SSH_KEYFILE

if [ -e $SSH_CONFIG_BACKUP ]
then
  rm -f $SSH_CONFIG
  mv $SSH_CONFIG_BACKUP $SSH_CONFIG
  chmod 600 $SSH_CONFIG
fi

if [ -e $KNOWN_HOSTS_BACKUP ]
then
  rm -f $KNOWN_HOSTS
  mv $KNOWN_HOSTS_BACKUP $KNOWN_HOSTS
  chmod 644 $KNOWN_HOSTS
fi
