#!/usr/bin/env bash

set -e -o pipefail

echo "[INFO] Updating plugins"

git submodule update --init --remote

gh_mail=$( vault kv get -field=value machineuser/GH_MAIL )
gh_name=$( vault kv get -field=value machineuser/GH_NAME )

# in case of the changes we need to commit them
changed=$( git --work-tree=. status --porcelain )

if [ -n "${changed}" ]; then
  echo "gh_mail: ${gh_mail}"
  echo "gh_name: ${gh_name}"
  git config --global user.email "${gh_mail}"
  git config --global user.name "${gh_name}"
  git commit -am"Update to the latest plugin revisions"
  git push --set-upstream origin $( git rev-parse --abbrev-ref HEAD )
fi
