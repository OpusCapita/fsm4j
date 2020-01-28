#!/usr/bin/env bash

set -eo pipefail

echo "[INFO] Cleanup local repository before saving its content into cache"

git_repo_dir=$( git rev-parse --show-toplevel )

du -sh ~/.m2/repository

# removing our application/module artifact (it just takes additional space but unused)
group_id=$( echo $( properties get -f $git_repo_dir/grails-app/conf/Config.groovy -k grails.project.groupId ) | sed -e 's/^"//' -e 's/"$//' )
echo "Group ID: $group_id"

rm -rfv ~/.m2/repository/${group_id//.//}

# removing SNAPSHOT libraries as they are mutable in time and needs to de loaded again each time when it is required
find ~/.m2/repository -type d -name '*-SNAPSHOT' -prune -exec rm -rf {} +

du -sh ~/.m2/repository

echo "[INFO] Remove installed SNAPSHOT plugins before saving them into cache"

app_name=$( properties get --file $git_repo_dir/application.properties --key app.name )
rm -rf target/projects/${app_name}/plugins/*-SNAPSHOT
