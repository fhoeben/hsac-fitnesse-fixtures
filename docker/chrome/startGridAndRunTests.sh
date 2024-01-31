#!/usr/bin/env sh

set -e

if [ "${CHANGE_FITNESSE_DIRECTOY_OWNER}" = "true" ]; then
  # ensure user of container is owner of all mounted volumes
  owner="$(whoami)"
  user_id="$(id -u "${owner}")"
  group_id="$(id -g "${owner}")"
  sudo chown -R "${user_id}:${group_id}" /fitnesse/
fi

/opt/bin/entry_point.sh &
while [ ! -e /tmp/.X99-lock ]; do sleep 0.1; done

./runTests.sh "$@"
