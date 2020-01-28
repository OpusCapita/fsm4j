#!/usr/bin/env bash

set -e -o pipefail

script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

normalize_kube_resource_name() {
  echo $1 | iconv -t ascii//TRANSLIT | sed -E s/[^a-zA-Z0-9]+/-/g | sed -E s/^-+\|-+$//g | tr A-Z a-z | cut -c1-53
}

azure_user=$( vault kv get -field=value "machineuser/AZURE_USER" )
azure_password=$( vault kv get -field=value "machineuser/AZURE_PASS" )
azure_subscription=$( vault kv get -field=value "machineuser/MINSK_CORE_AZURE_SUBSCRIPTION_ID" )
azure_kube_cluster_name=$( vault kv get -field=value "machineuser/MINSK_CORE_K8S_AZURE_NAME" )
azure_kube_cluster_resource_group=$( vault kv get -field=value "machineuser/MINSK_CORE_K8S_AZURE_RG" )

#==============================
echo "[INFO] Login to Azure"
az login -u "$azure_user" -p "$azure_password"
az account set -s "$azure_subscription"

#==============================
echo "[INFO] Login to Kubernetes cluster"
az aks get-credentials -g "$azure_kube_cluster_resource_group" -n "$azure_kube_cluster_name"

# get project name
github_project_name=$($script_dir/../get-github-repo-name.sh)
# get branch name
git_branch=$( git rev-parse --abbrev-ref HEAD )
# get namespace name
deployment_namespace="dev-${github_project_name}-$( normalize_kube_resource_name $git_branch )"

# searching for a release in the namespace
release=$(helm ls --short --namespace $deployment_namespace)

if [ -z "$release" ]
then
  echo "Release is not found in namespace $deployment_namespace"
else
  helm delete --purge $release
fi

# deleting namespace
kubectl delete namespace $deployment_namespace --ignore-not-found=true
