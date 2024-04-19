# ArangoDB Java Health Check

This program executes at intervals of configurable duration a `getVersion()` request to ArangoDB.
On failure, it exits with exit code 1.

## requirements
- 
- Java 11+
- Maven

## build

 ```
 mvn install
 ```

This creates the docker image `arangodb/java-health-check:1.0.0`.

## config

The docker image accepts the following environment variables:
- ADB_LOG_LEVEL: `ALL`|`TRACE`|`DEBUG`|`INFO`|`WARN`|`ERROR`|`OFF`
- ADB_CHECK_INTERVAL_MS: default `1000`
- ADB_USER: default `root`
- ADB_PASSWORD:
- ADB_ENDPOINTS: comma-separated list of `host:port` pairs, e.g. `coordinator1:8529,coordinator2:8529`
- ADB_PROTOCOL: `VST`|`HTTP_JSON`|`HTTP_VPACK`|`HTTP2_JSON`|`HTTP2_VPACK`, default `HTTP2_JSON`
- ADB_USE_SSL: `true`|`false`
- ADB_VERIFY_HOST: `true`|`false`
- ADB_CERT: base64 encoded cert

For example:

```
docker run \
  -e ADB_LOG_LEVEL=INFO \
  -e ADB_PASSWORD=test \
  -e ADB_ENDPOINTS=172.28.0.1:8529 \
  -e ADB_USE_SSL=true \
  -e ADB_CERT=$ADB_CERT \
  -e ADB_VERIFY_HOST=false \
  arangodb/java-health-check:1.0.0
```
