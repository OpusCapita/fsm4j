#!/usr/bin/env bash

set -eo pipefail

# Expects the following environment variables to exist:
#
# CATALINA_HOME - Tomcat directory
# APP_ROOT - Application directory

while [ $# -gt 0 ]; do
  case "$1" in
    --context-path=*)             # required
      CONTEXT_PATH="${1#*=}"
      ;;
    --java-opts=*)               # optional - additional Java options
      EXTRA_JAVA_OPTS="${1#*=}"
      ;;
    *)
      printf "Error: Invalid argument ${1}\n"
      exit 1
  esac
  shift
done

assert_vars() {
  for v in $@; do
    # if one of required variables is empty
    if [[ -z "${!v}" ]]; then
      printf "$(basename $0): Error: $v is empty\n"
      exit 1
    fi
  done
}

printf "Check if --context-path are specified... "
assert_vars CONTEXT_PATH

# link app to context path
# Make /a/b/c -> a#b#c
ESCAPED_CONTEXT_PATH=${CONTEXT_PATH#*/}
ESCAPED_CONTEXT_PATH="${ESCAPED_CONTEXT_PATH//\//#}"
ln -s "$APP_ROOT" "$CATALINA_HOME/webapps/$ESCAPED_CONTEXT_PATH"

printf "\nwebapps:\n"
ls -la "$CATALINA_HOME/webapps"

#####################################################################
#
# Run Tomcat
#
#####################################################################

COMMON_JAVA_OPTS="-verbose:gc -XX:+PrintGCDetails -Xloggc:$CATALINA_HOME/logs/gc.log -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+HeapDumpOnOutOfMemoryError -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xms2g -Xmx2g -XX:MaxMetaspaceSize=1024m -XX:ReservedCodeCacheSize=256m -XX:+UseCodeCacheFlushing -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -XX:+UseG1GC"

# Make sure to pass $JAVA_OPTS in the end to make it rewriteable
JAVA_OPTS=`printf "%s %s" "$COMMON_JAVA_OPTS" "$EXTRA_JAVA_OPTS"`
printf "JAVA_OPTS: $JAVA_OPTS\n"
export JAVA_OPTS

exec catalina run
