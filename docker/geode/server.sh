#!/bin/bash

echo "Sleep to give locator the chance to be ready ..."
sleep 30

NAME=geode-server
gfsh  -e "start server --name=${NAME} --group=turns --bind-address=${NAME} --http-service-bind-address=${NAME} --hostname-for-clients=${NAME} --server-port=40404 --locators=geode-locator[10334]" \
      -e "connect --locator=geode-locator[10334]" \
      -e "create region --name=moves --type=REPLICATE_PERSISTENT" \
      -e "create region --name=requests --type=REPLICATE_PERSISTENT" \
      -e "create index --name=playerIdIdx --expression=playerId --region=/requests" \
      -e "create region --name=player-locks --type=REPLICATE_PERSISTENT"

echo "Sleeping continuously to keep the container going ..."
while true; do sleep 1h; done