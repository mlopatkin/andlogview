#!/usr/bin/env python

"""
Checks some basic sanity constraints about the code: proper eolns, no tabs, etc.
"""

#  Copyright 2020 Mikhail Lopatkin
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import argparse
import collections
import fnmatch
import os
import sys

Error = collections.namedtuple('Error', ['file', 'line', 'char', 'msg'])

CRLF_ONLY_GLOBS = ['*.bat']


def matches_any(filename, globs):
    return any(fnmatch.fnmatch(filename, g) for g in globs)


def split_eol(line_with_eol):
    if line_with_eol.endswith(b'\r\n'):
        return line_with_eol[:-2], line_with_eol[-2:]
    if line_with_eol.endswith(b'\n'):
        return line_with_eol[:-1], line_with_eol[-1:]
    return line_with_eol, b''


class FileChecker(object):
    def __init__(self, filename):
        self.filename = filename
        self.eol = b'\n'
        if matches_any(self.filename, CRLF_ONLY_GLOBS):
            self.eol = b'\r\n'
        self.errors = collections.deque()
        self._has_tabs = False
        self._has_wrong_eol = False

    def run_checks(self):
        with open(self.filename, 'rb') as infile:
            for line_no, line in enumerate(infile, 1):
                self.line_no = line_no
                self.line = line
                self.check_tabs()
                self.check_eol_and_trailing_whitespace()
        return self.has_errors

    def check_tabs(self):
        if self._has_tabs:
            # Skip if more than one tab in file
            return

        tab_index = self.line.find(b'\t')
        if tab_index >= 0:
            self._has_tabs = True
            self.error(
                tab_index + 1, 'One or more TAB characters in file. Only the '
                'first is reported')

    def check_eol_and_trailing_whitespace(self):
        # For efficiency both eol and trailing whitespace are checked at once
        # eol technically also is a whitespace character
        first_trailing_whitespace = len(self.line.rstrip())
        trail, eol = split_eol(self.line[first_trailing_whitespace:])
        if trail:
            self.error(first_trailing_whitespace, 'Trailing whitespace')
        if not eol:
            self.error(len(self.line), 'Last line must ends with EOL')
        elif not self._has_wrong_eol and self.eol != eol:
            self._has_wrong_eol = True
            self.error(
                len(self.line) - len(eol),
                'Expected {} but got {}'.format(repr(self.eol), repr(eol)))

    def error(self, char_at, msg):
        self.errors.append(Error(self.filename, self.line_no, char_at, msg))

    @property
    def has_errors(self):
        return len(self.errors) > 0

    def print_errors(self):
        for e in self.errors:
            print(':'.join(str(p) for p in e))


def filter_ignored_files(ignored_files_list_filename, files):
    with open(ignored_files_list_filename, 'r') as inp:
        # Allow comments
        ignored_files_masks = [
            ss for ss in (s.strip() for s in inp.readlines())
            if ss and not ss.startswith('#')
        ]
    files_set = set(files)
    for mask in ignored_files_masks:
        files_set.difference_update(fnmatch.filter(files_set, mask))
    # Re-iterate to keep order intact.
    return [f for f in files if f in files_set]


def parse_args():
    p = argparse.ArgumentParser()
    p.add_argument('--files',
                   nargs='*',
                   required=True,
                   help='List of files to check')
    p.add_argument('--ignored-files-list',
                   type=os.path.realpath,
                   help='List of files/globs to skip when checking')
    return p.parse_args()


def main():
    args = parse_args()
    has_errors = False
    if args.ignored_files_list:
        args.files = filter_ignored_files(args.ignored_files_list, args.files)
    for filename in args.files:
        checker = FileChecker(filename)
        # Order of arguments is important here, checks must run for all files
        # even if some errors have been found.
        has_errors = checker.run_checks() or has_errors
        checker.print_errors()

    return 0 if not has_errors else 1


if __name__ == "__main__":
    sys.exit(main())
