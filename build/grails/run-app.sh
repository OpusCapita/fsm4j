#!/usr/bin/env bash

set -e -o pipefail

echo "[INFO] Start application"

script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

. $script_dir/configure-maven-and-grails.sh
grails -reloading run-app --stacktrace --verbose
