This server mocks some of the back end servers to load test our load balancer without the need for expensive backend servers.
We have two implementations currently: Java and Node.js.

## Java

Currently only the async vrp endpoint is mocked: https://docs.graphhopper.com/#operation/asyncVRP

### Build

```
mvn clean package
```

### Run with Java

```bash
java -jar target/fake-server.jar [-conf vertx.json]
```

### Run with Docker

```bash
docker run -p 8080:8080 graphhopper/graphhopper-load-test-fake-backend
```

### Run with Docker Compose

```bash
docker-compose up
```

### Example Configuration

Default port 8080 can be overwritten in the configuration:

```json
{
  "http.port" : 4321
}
```

## Node.js

To run the server in Node.js, run the following:

    docker run -d --rm --network host --name vrp-fake -v "$PWD"/node:/usr/src/app -w /usr/src/app node:8 node server.js [port]

This will run a Node 8 Docker container, pass in the `node/server.js` file and run that on the selected port.
