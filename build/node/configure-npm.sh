#!/usr/bin/env bash

set -eo pipefail

>&2 echo "[INFO] Configure npm"

file=$(mktemp)

if [ ! -f /.dockerenv ]; then
  >&2 echo "[WARNING] Skipping $(basename $0) execution, it will be run only inside Docker container because it can override local user's settings"
else
  # Get values required by ~/.npmrc
  cat << EOF > $file
export NPM_DEFAULT_REGISTRY=$( vault kv get -field=value machineuser/NPM_DEFAULT_REGISTRY )
export NPM_DEFAULT_REGISTRY_AUTH=$( vault kv get -field=value machineuser/NPM_DEFAULT_REGISTRY_AUTH )
export NPM_DEFAULT_REGISTRY_MAIL=$( vault kv get -field=value machineuser/NPM_DEFAULT_REGISTRY_MAIL )
export NPMJS_ORG_AUTH=$( vault kv get -field=value machineuser/NPMJS_ORG_AUTH )
EOF
fi

echo $file
