# turns-choreography

This project shows how we can use a mixture of synchronous REST API and asynchronous messaging based choreography with spring-boot microservices. There are 4 components in this project. All of them as spring-boot apps. They are:

- *api* - Listens to synchronous REST API by a *player* to take a *turn*. The *turn* can consist of one or more *moves*. The *moves* within a *turn* have to be performed in the same order as specified in the request. There is a timestamp associated with each *turn*. Each *turn* for a player has to be performed in the order of that timestamp. Also, two *turns* for the same player can't occur at the same time. This component ensures that *moves* by players are sequential by relying on distributed locking. It emits *move-requested* and *turn-completed* events and listens to *move-completed* and *turn-completed* events. 
- *choreographer* - Performs a *move* by going through the four *steps* every *move* goes through. Listens and responds to asynchronous events via spring-cloud-stream to *move-requested* and responds with *move-completed* when done. During the *move* choroegraphy, emits *xxxx-requested* event and listens to *xxxx-completed* events where *xxxx* is the name of the step.
- *worker* - Workers that perform work required for a step. For demo purposes the same worker codebase is used to represent 4 different kinds of step types (left, right, forward, and back). Listens and responds to asynchronous events via spring-cloud-stream 
- *requester* - Requester app to generate lots of random player *turn* requests that are sent to the REST API  

By default, the following ports are used:

| Service | Port | Important URLs |
| ----- | ---- | ------ |
| requester | `8181` | `POST /requests`<br/>`GET /swagger-ui.html` |
| api | `8484` | `POST /turns`<br/>`GET /swagger-ui.html`<br/>`GET /actuator/prometheus` |
| choreographer | `8585` | `GET /swagger-ui.html` |

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

The app will pick between RabbitMQ or Apache Kafka depending on which spring profile is in effect. Also, the apps will pick between Redis or Apache Geode depending on the spring profiles in effect. The following combinations are possible:

- RabbitMQ + Apache Geode
- RabbitMQ + Redis
- Apache Kafka + Apache Geode
- Apache Kafka + Redis

The username/password to access this RabbitMQ dashboard instance would be `guest`/`guest`

By default, the following ports are used by the services:

| Service | Port | Important URLs |
| ----- | ---- | ------ |
| RabbitMQ | `5672`, `15672` | Admin dashboard at port `15672` with `GET /` - Use `guest`/`guest` as username/password |
| Apache Kafka | `2181`, `9092` | There is no admin UI |
| Apache Geode - Locator | `10334` | There is no admin UI, can do admin activities using `gfsh` command within the container |
| Apache Geode - Server | `40404` | There is no admin UI |
| Redis | `6379` | There is no admin UI, can use `redis-cli` within the container to perform admin activities |
| Prometheus | `9090` | Admin dashboard with `GET /` |
| Grafana | `3000` | Dashboards with `GET /`. Don't require authentication unless you want to change/create a dashboard - use `admin`/`admin` as username/password. There are couple of dashboards built in.

### Start the services

API:

```bash
cd api
./gradlew bootRun
```

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

