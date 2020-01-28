#!/usr/bin/env bash

set -eo pipefail

>&2 echo "[INFO] Configure maven"

file=$(mktemp)

if [ ! -f /.dockerenv ]; then
  >&2 echo "[WARNING] Skipping $(basename $0) execution, it will be run only inside Docker container because it can override local user's settings"
else
  # Get values required by ~/.m2/settings.xml and ~/.grails/settings.groovy
  cat << EOF > $file
export MAVEN_REPO=$( vault kv get -field=value machineuser/MAVEN_REPO )
export JFROG_USER=$( vault kv get -field=value machineuser/JFROG_USER )
export JFROG_PASSWD=$( vault kv get -field=value machineuser/JFROG_PASSWD )
EOF
fi

echo $file
