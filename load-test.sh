#!/bin/bash

LOAD='[
  { "arrivalRate": 5,   "durationSeconds": 15 },
  { "arrivalRate": 10,  "durationSeconds": 15 },
  { "arrivalRate": 20,  "durationSeconds": 15 },
  { "arrivalRate": 30,  "durationSeconds": 15 },
  { "arrivalRate": 40,  "durationSeconds": 15 },
  { "arrivalRate": 50,  "durationSeconds": 15 },
  { "arrivalRate": 60,  "durationSeconds": 15 },
  { "arrivalRate": 70,  "durationSeconds": 15 },
  { "arrivalRate": 80,  "durationSeconds": 15 },
  { "arrivalRate": 90,  "durationSeconds": 15 },
  { "arrivalRate": 100, "durationSeconds": 15 },
  { "arrivalRate": 110, "durationSeconds": 15 },
  { "arrivalRate": 120, "durationSeconds": 15 },
  { "arrivalRate": 130, "durationSeconds": 15 },
  { "arrivalRate": 140, "durationSeconds": 15 },
  { "arrivalRate": 150, "durationSeconds": 15 },
  { "arrivalRate": 160, "durationSeconds": 15 },
  { "arrivalRate": 170, "durationSeconds": 15 },
  { "arrivalRate": 180, "durationSeconds": 15 },
  { "arrivalRate": 190, "durationSeconds": 15 },
  { "arrivalRate": 200, "durationSeconds": 150 }
]'

curl -X POST 'http://localhost:8181/requests' -H 'Content-Type: application/json' -d "${LOAD}"
curl -X POST 'http://localhost:8282/requests' -H 'Content-Type: application/json' -d "${LOAD}"
