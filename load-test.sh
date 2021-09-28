#!/bin/bash

LOAD='[
  {
    "arrivalRate": 5,
    "durationSeconds": 30
  },
  {
    "arrivalRate": 25,
    "durationSeconds": 30
  },
  {
    "arrivalRate": 50,
    "durationSeconds": 30
  },
  {
    "arrivalRate": 75,
    "durationSeconds": 60
  },
  {
    "arrivalRate": 100,
    "durationSeconds": 60
  },
  {
    "arrivalRate": 100,
    "durationSeconds": 60
  },
  {
    "arrivalRate": 125,
    "durationSeconds": 60
  }
]'

curl -X POST 'http://localhost:8181/requests' -H 'Content-Type: application/json' -d "${LOAD}"
curl -X POST 'http://localhost:8282/requests' -H 'Content-Type: application/json' -d "${LOAD}"
