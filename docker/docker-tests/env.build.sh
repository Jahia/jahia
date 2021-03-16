#!/bin/bash
# This script can be used to manually build the docker images necessary to run the tests
# It should be executed from the tests folder

# It assumes that you previously built the module you're going to be testing
#   and that the modules artifacts are located one level up

if [ ! -d ./artifacts ]; then
  mkdir -p ./artifacts
fi

docker build -t jahia/jahia-core_test:latest .