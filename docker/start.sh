#!/bin/bash

docker-compose \
  --project-name ping-pong \
  --project-directory docker \
  -f docker-compose-kafka.yml \
  -f docker-compose-db.yml \
  up --build --remove-orphans -d \
  postgres zookeeper kafka
