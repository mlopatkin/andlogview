#!/bin/sh

git ls-files -z | xargs -0 \
  python tools/hooks/lint.py --ignored-files-list .nolint --files || exit 1
