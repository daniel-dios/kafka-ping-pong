# 🏓 kafka-ping-pong
## 🤓 Explanation:

This is a simple consumer-producer of Kafka messages. Using spring cloud streams, spring data as infra.
All the logic about business, idempotency, where to store (send) messages, to drop or call different services (image processor) is implemented in a single-use case based on hexagonal architecture implemented with Java15 and unitary tested. Easy to test and to evolve.

## 🧪 Tests

All the code was developed with the TDD approach, you will see here unitary tests, module/integration tests, and a real end to end with everything dockerized and only using Kafka to check outputs in different scenarios.

## 🤔 Doubts?
Why not everything in infra? 

- I was considering at the beginning using KSQL and resolve everything in infra code but you may change your consumer for a rest controller tomorrow or just a command in your terminal and you will keep this logic into a simple use case as a piece of your business. 
- Also I followed the dependency injection, so now, we will be able to substitute the image processor or the repositories with another implementation and the code will be the same.

Is it weird to persist twice the messages?

- Maybe, but it is the only way to keep messages and handle idempotency logic (that could change on feature) force to you to have in a repo at least all the error messages and the last success. (If you don't understand this, check the description) So, if code persists in memory and the producer fails, everything works as a transaction and DB changes won't be committed. Also, the consumption of the message won't is committed too.

Where are the configurations?

- You will see Kafka host and port on the application.yml and the number of consecutive errors on the Config.class (we can refactor in a .yml too but I have my doubt if you won't need a new deploy to change it).

## 🏃 Instructions to run:

- There is a Dockerfile on the root directory in order to build an image of the service. In order to have all dependencies there is a docker-compose that will run the infra needed and the service itself, so just run in your favorite terminal:
``` 
docker-compose up/down 
```

- If you just want to run locally the service (with proper configurations on application.yml or application-yourconfig.yml and springprofiles=yourconfig just execute:
``` 
./gradlew bootRun -x test 
```

- Or, if you have no kafka+zookeeper+postgres you may need to deploy first the dependencies with:
```
./docker/start.sh
./gradlew bootRun -x test 
```


## 📝 Specifications:

The service will respond for success and error processing on different topics. To simulate error conditions, adding a field: force_error: true to the event payload is allowed.

Inbound and outbound topics shall be configurable.

Take into consideration that the service should be idempotent on the success branch, this is:

- If a given message is received more than once, then the service shall respond with the last response without attempting to process the message, but only if the previous time the response was successful

- If a given message is received more than once and the previous attempt to process the message failed, then the service shall try to reprocess the message and respond accordingly.

- If a given message was received and failed more than a configurable amount of times, then the message is not reattempted and sent to a dead letter queue topic. No future request to process the same message shall be considered.

Te service will listen for a ping in *ping* topic:

```
{
“transaction-id”: TRANSACTION-ID,
“payload”: {
  “message”: “ping”,
  “force_error”: false
  }
}
```

And responds (produces) with a pong message in *pong* topic:

```
{
“transaction-id”: TRANSACTION-ID,
“payload”: {
  “message”: “pong”,
  “processing_time”: TIME
  }
}
```


Where:

- TRANSACTION-ID is a randomly generated UUID
- TIME is the time the service took to generate the response.*

* (will be 30+computation time in case 1st time receiving the message or just computation time if the message was received before)
