#!/usr/bin/env bash

set -euo pipefail

echo "[INFO] Publish technical documentation"

################
# download jar #
################

GH_TOKEN="$( vault kv get -field=value machineuser/GH_TOKEN )"

VERSION="1.0.4"

ASSET_ID=$(curl -sSL -H "Authorization: token ${GH_TOKEN}" https://api.github.com/repos/OpusCapita/technical-documentation-rsync-to-azure/releases \
| jq ".[] | select(.name == \"$VERSION\")| .assets | select(.[] | .browser_download_url | endswith(\".jar\")) | .[0].id")

wget -q --auth-no-challenge --header='Accept:application/octet-stream' \
  https://$GH_TOKEN:@api.github.com/repos/OpusCapita/technical-documentation-rsync-to-azure/releases/assets/$ASSET_ID \
  -O /usr/local/bin/technicalDocumentationRsyncToAzure.jar

#############
# setup env #
#############

mkdir -p ~/.ssh

if [ ! -f /.dockerenv ]; then
  echo "[WARNING] SSH 'Host technical-documentation' configuration will be applied inside Docker container because it can override local user's settings"
fi

if [ -f /.dockerenv ]; then
  if [ ! -e ~/.ssh/opuscapita-technical-documentation.key ]; then
    echo "$( vault kv get -field=value machineuser/SSH_KEY )" > ~/.ssh/opuscapita-technical-documentation.key

    chmod og-rwx ~/.ssh/opuscapita-technical-documentation.key

    # add SSH key as identity for technical-documentation host, set hostname to storage host
    echo "Host technical-documentation
     HostName $( vault kv get -field=value machineuser/DOCUMENTATION_STORAGE_HOST )
     User root
     IdentityFile ~/.ssh/opuscapita-technical-documentation.key
     StrictHostKeyChecking no
      " >> ~/.ssh/config
  fi
fi

if [[ ! -d "grails-app" ]]
then
  echo "Error: 'grails-app' directory is not found -> current folder does not contain grails project sources"
  exit 1
fi

if ls *GrailsPlugin.groovy >/dev/null 2>&1; then
  echo "current directory will be considered as grails plugin sources"
  GRAILS_ARGUMENT="--grails-plugin"
else
  echo "current directory will be considered as grails application sources"
  GRAILS_ARGUMENT="--grails-application"
fi

java -jar /usr/local/bin/technicalDocumentationRsyncToAzure.jar $GRAILS_ARGUMENT
