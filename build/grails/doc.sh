#!/usr/bin/env bash

set -e -o pipefail

echo "[INFO] Build technical documentation"

rm -rf plugin.xml

script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
. $script_dir/configure-maven-and-grails.sh

grails doc --offline
