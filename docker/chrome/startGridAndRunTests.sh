#!/usr/bin/env sh

set -e

/opt/bin/entry_point.sh &
while [ ! -e /tmp/.X11-unix/X99 ]; do sleep 0.1; done

./runTests.sh $@
