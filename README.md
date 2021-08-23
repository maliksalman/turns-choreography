# turns-choreography

This project shows how we can use messaging based choregraphy with spring-boot microservices. There is one choreographer service and 4 worker services. 

The choreographer is contact via synchronous REST API by a "player" to take a "turn". The turn can consist of one or more "moves". The moves within a turn have to be performed in the same order as specified in the request. There is a timestamp associated with each turn. Each turn for a player have to be performed in the order of that timestamp.

There can be multiple instances of each of the microservice.

### Start RabbitMQ

The following command will start RabbitMQ running on port 5672 with the admin console available at http://localhost:15672

```
docker run --rm -d -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

The username/password to access this RabbitMQ dashboard instance would be `guest`/`guest`

### Start the services

Choreographer:

```bash
cd choreographer
./gradlew bootRun
```

Workers:

```bash
cd worker
SPRING_PROFILES_ACTIVE=breathe ./gradlew bootRun
SPRING_PROFILES_ACTIVE=think ./gradlew bootRun
SPRING_PROFILES_ACTIVE=act ./gradlew bootRun
SPRING_PROFILES_ACTIVE=react ./gradlew bootRun
```