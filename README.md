This server mocks some of the back end servers to load test our load balancer without the need for expensive backend servers.

Currently only the async vrp endpoint is mocked: https://docs.graphhopper.com/#operation/asyncVRP

## Build

```
mvn clean package
```

## Run

```bash
java -jar target/fake-server.jar [-conf vertx.json]
```

## Example Configuration

```json
{
  "http.port" : 4321
}
```
