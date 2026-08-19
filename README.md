# Description

This repository is based on the demo code of a bug found in Spring Integration IP v6.5.7 (`org.springframework.integration:spring-integration-ip:6.5.7`). The original demo code is archived in the repository https://github.com/emuller84/spring-integration-ip-bug-demo-20260312 . The goal is to keep updating this project with new Spring Integration IP releases to test for similar bugs. Git tags and/or branches will be created for each tested version.

# Contents of this repository

- Spring boot application: the root of this repository contains a Java, Gradle-based, project. It's an HTTP JSON API project using spring boot and spring integration that mimics the TCP connectivity used by the real-world application were the original bug being investigated actually happened.
- [`device_simulator.py`](device_simulator.py): a python 3 script the creates the TCP server that the spring application will connect to.
- [`spring-integration-ip-bug-regression-test.postman_collection.json`](spring-integration-ip-bug-regression-test.postman_collection.json): a Postman collection with a sample request to hit the spring application
  - You can use Postman's "performance run" on this collection to hit the application with high TPS and, hopefully, trigger the error.

# API request

A request to the API application looks like this:

```text
POST http://localhost:8014/v0/api/endpointA
```

```json
{
  "value": "this is a test"
}
```

And a successful response:

```json
{
  "deviceResponseValue": "DEVICE RESPONSE - ECHO: this is a test",
  "deviceError": null,
  "otherError": null
}
```

# Steps

1. Launch the TCP server: `python3 device_simulator.py`
2. Launch the spring application
   - This spring app uses two TCP connections (two `TcpOutboundGateway`s plus connection factories), which may increase the chances of finding an error. If you want to try it with just one connection, which I guess would be easier to debug, then set the configuration property `device-service.enable-second-connection` to `false` in [src/main/resources/application.yaml](src/main/resources/application.yaml).
3. On the Postman application:
   - Import the [`spring-integration-ip-bug-regression-test.postman_collection.json`](spring-integration-ip-bug-regression-test.postman_collection.json) collection
   - Go to the "Overview" page of the imported collection -> "Runs" -> "Performance" -> "Run" -> "Performance" and run
     - A "fixed" load profile and "20" virtual users should work
     - Let it run for some minutes to increase the probabilities of finding an error
