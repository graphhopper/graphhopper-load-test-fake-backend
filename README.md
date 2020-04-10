## Build

mvn clean package

## Run

```bash
java -jar target/fake-server.jar [-conf vertx.json]
```

Currently only the async vrp endpoint is mocked: https://docs.graphhopper.com/#operation/asyncVRP

## Example Configuration

```json
{
  "http.port" : 4321
}
```
