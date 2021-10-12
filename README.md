# turns-choreography

This project shows how we can use a mixture of synchronous REST API and asynchronous messaging based choreography with spring-boot microservices. There are 4 components in this project. All of them as spring-boot apps. They are:

- *api* - Listens to synchronous REST API by a *player* to take a *turn*. A *turn* can consist of one or more *moves*. The *moves* within a *turn* have to be performed in the same order as specified in the request. There is a timestamp associated with each *turn*. Each *turn* for a player has to be performed in the order of that timestamp. Also, two *turns* for the same player can't occur at the same time. This component ensures that *moves* by players are processed sequentially by relying on distributed locking. It emits *move-requested* and *turn-completed* events and listens to *move-completed* and *turn-completed* events. 
- *choreographer* - Performs a *move* by going through the four *steps* every *move* goes through. Listens and responds to asynchronous events via spring-cloud-stream to *move-requested* and responds with *move-completed* when done. During the *move* choreography, emits *xxxx-requested* event and listens to *xxxx-completed* events where *xxxx* is the name of the step (`left`, `right`, `forward`, and `back`).
- *worker* - Workers that perform work required for a step. For demo purposes the same worker codebase is used to represent 4 different kinds of step types (`left`, `right`, `forward`, and `back`). Listens and responds to asynchronous events via spring-cloud-stream. At a minimum, we run one instance of each kind of step.
- *requester* - Requester app to generate lots of random player *turn* requests that are sent to the REST API  

By default, the following ports are used:

| Service | Port | Prometheus Metrics | Swagger | Important URLs |
| ----- | ---- | ------ | ------ | ------ |
| requester | `8181` | | `/swagger-ui.html` | `POST /requests` |
| api | `8484` | `/actuator/prometheus` | `/swagger-ui.html` | `POST /turns` | 
| choreographer | `8585` | `/actuator/prometheus` | | |

There can be multiple instances of each of the microservice running.

### Start the supporting services

```
cd docker
docker-compose up -d
```

This will start few services:

- RabbitMQ
- Apache Kafka
- Apache Geode
- Redis
- Grafana
- Prometheus

For **asynchronous messaging**, the apps will pick between RabbitMQ or Apache Kafka depending on which spring profile is in effect. For temporary **fast storage** and **distributed locking**, the apps will pick between Redis or Apache Geode depending on the spring profiles in effect. The following combinations are possible:

| | Apache Kafka | RabbitMQ | Spring Profiles to activate |
| ---- | :------: | :-----: | ------- |
| Apache Geode | | x | `geode` |
| Apache Geode | x | | `geode,kafka` |
| Redis | | x | `redisson` |
| Redis | x | | `redisson,kafka` |

By default, the following ports are used by the services:

| Service | Port | Important URLs |
| ----- | ---- | ------ |
| RabbitMQ | `5672`, `15672` | Admin dashboard at port `15672` with `GET /` - Use `guest`/`guest` as username/password |
| Apache Kafka | `2181`, `9092` | There is no admin UI |
| Apache Geode - Locator | `10334` | There is no admin UI, can do admin activities using `gfsh` command within the container |
| Apache Geode - Server | `40404` | There is no admin UI |
| Redis | `6379` | There is no admin UI, can use `redis-cli` command within the container to perform admin activities |
| Prometheus | `9090` | Admin dashboard with `GET /` |
| Grafana | `3000` | Dashboards with `GET /`. Does not require authentication unless you want to change/create a dashboard - use `admin`/`admin` as username/password. There are a couple of dashboards built in.

### Start the apps

Assuming we want to use Apache Kafka for synchronous messaging and Apache geode for fast storage, we would issue the following commands to start the apps:

API:

```bash
cd api
SPRING_PROFILES_ACTIVE=geode,kafka ./gradlew bootRun
```

Choreographer:

```bash
cd choreographer
SPRING_PROFILES_ACTIVE=geode,kafka ./gradlew bootRun
```

Workers:

```bash
cd worker
SPRING_PROFILES_ACTIVE=kafka,forward ./gradlew bootRun
SPRING_PROFILES_ACTIVE=kafka,back    ./gradlew bootRun
SPRING_PROFILES_ACTIVE=kafka,left    ./gradlew bootRun
SPRING_PROFILES_ACTIVE=kafka,right   ./gradlew bootRun
```

Requester:

```bash
cd requester
./gradlew bootRun
```
