#!/bin/bash

NAME=geode-locator
gfsh  -e "start locator --name=${NAME} --connect=false --redirect-output --bind-address=${NAME} --http-service-bind-address=${NAME} --jmx-manager-hostname-for-clients=${NAME} --hostname-for-clients=${NAME}" \
      -e "connect --locator=${NAME}[10334]" \
      -e "configure pdx --disk-store=DEFAULT"

echo "Sleeping continuously to keep the container going ..."
while true; do sleep 1h; done