#!/usr/bin/env bash

set -eo pipefail

echo "[INFO] Building Docker image"

script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

git_repo_dir=$( git rev-parse --show-toplevel )

app_name=$( properties get --file $git_repo_dir/demo/server/application.properties --key app.name )

app_version=$( properties get --file $git_repo_dir/demo/server/application.properties --key app.version )

docker_repository="$( . $script_dir/../get-docker-repository.sh )"

docker_tag="$( . $script_dir/../get-docker-tag.sh )"

# Get Github project fullname

github_project=$($script_dir/../../get-github-repo-owner-slash-name.sh)

revision=$( git rev-parse --verify HEAD )

cd $git_repo_dir

docker build \
  --build-arg WAR_PATH="./demo/server/target/${app_name}-${app_version}.war" \
  --build-arg NAME="$app_name" \
  --build-arg VERSION="$app_version" \
  --build-arg CREATED=$( date -u +"%Y-%m-%dT%H:%M:%SZ" ) \
  --build-arg SOURCE="https://github.com/$github_project" \
  --build-arg REVISION="$revision" \
  -t "$docker_repository":"$docker_tag" \
  -f $script_dir/Dockerfile \
  $git_repo_dir
