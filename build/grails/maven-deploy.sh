#!/usr/bin/env bash

set -e -o pipefail

echo "[INFO] Build and deploy artifacts to Maven repository"

script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

. $script_dir/configure-maven-and-grails.sh
grails maven-deploy -Dgrails.env=prod -verbose -offline
