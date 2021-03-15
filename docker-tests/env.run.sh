#!/bin/bash
# This script can be used to warmup the environment and execute the tests
# It is used by the docker image at startup

if [[ ! -f .env ]]; then
 cp .env.example .env
fi

source .env

#!/usr/bin/env bash
START_TIME=$SECONDS

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

echo " == Using JAHIA_URL= ${JAHIA_URL}"

echo " == Waiting for Jahia to startup"
./node_modules/jahia-reporter/bin/run utils:alive --jahiaUrl=${JAHIA_URL}
ELAPSED_TIME=$(($SECONDS - $START_TIME))
echo " == Jahia became alive in ${ELAPSED_TIME} seconds"

echo "== Run tests =="
CYPRESS_baseUrl=${JAHIA_URL} yarn e2e:ci
if [[ $? -eq 0 ]]; then
  echo "success" > $DIR/results/test_success
  exit 0
else
  echo "failure" > $DIR/results/test_failure
  exit 1
fi

# After the test ran, we're dropping a marker file to indicate if the test failed or succeeded based on the script test command exit code
