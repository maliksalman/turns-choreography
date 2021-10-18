#!/bin/bash

minikube status > /dev/null 2> /dev/null
if [[ $? -ne 0 ]]; then
  echo "Error: minikube is not running"
  exit
fi

eval $(minikube -p minikube docker-env)
pushd $PWD
BASEDIR=$(dirname $0)

for NAME in requester api choreographer worker; do
  pushd $BASEDIR/../$NAME
  ./gradlew clean bootBuildImage --imageName=turns/$NAME:1.0
  popd
done

popd