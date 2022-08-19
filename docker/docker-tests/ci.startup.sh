#!/usr/bin/env bash

# This script controls the startup of the container environment
# It can be used as an alternative to having docker-compose up started by the CI environment
source ./set-env.sh

echo " == Printing the most important environment variables"
echo " MANIFEST: ${MANIFEST}"
echo " JAHIA_IMAGE: ${JAHIA_IMAGE}"
echo " TESTS_IMAGE: ${TESTS_IMAGE}"
echo " DOCKER_COMPOSE_FILE: ${DOCKER_COMPOSE_FILE}"

docker-compose -f ${DOCKER_COMPOSE_FILE} up -d --renew-anon-volumes --remove-orphans --force-recreate database jahia jahia-browsing

if [[ $1 != "notests" ]]; then
    echo "$(date +'%d %B %Y - %k:%M') [TESTS] == Starting cypress tests =="
    docker-compose -f ${DOCKER_COMPOSE_FILE} up --abort-on-container-exit --renew-anon-volumes cypress
fi
