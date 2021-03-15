#!/bin/sh
set -e

until (nc -w 1 -v $1 $2 > /dev/null 2>&1 </dev/null) > /dev/null; do
  echo "Testing network access on host $1:$2... "
  sleep 2
done
