# Platform-Services

## Introduction

It can be said that the Space Service Platform of AO.space(SSP of AO.space) only provides the coordination and management of network resources (domain name, communication channel), so the data storage personal equipment belongs to private ownership. At the same time, the core functions of the SSP will be gradually open source and can be deployed privately. Personal devices can no longer rely on the official platform. Platform-Registry is responsible to manage the SSP of AO.space.

## System architecture

![SSP&Platform-Registr Arch.png](docs/en/asserts/SSP&Platform-Registr%20Arch.png)

The responsibility of SSP is to establish a transparent communication channel for personal equipment. It includes the Proxy Service, the Network Transit Server, and the Registry Service.

1. User-side Services: It is used to forward the requests from clients to AO.space.
- Proxy Service: provide high-availability forwarding and horizontal expansion support for the requests from clients.
- Network Transit Server: provides network support services that penetrate NAT access AO.space through relay forwarding.

2. The role of the management-side service is to provide the registration service of AO.space, and coordinate and manage the platform network resources (domain name, communication channel, etc.).

> **_ Note:_** For a complete deployment guide of the SSP, please refer to [AOPlatform Community Deployment Guide]（ https://ao.space/open/documentation/104002 ).

Platform-Registry is the implementation of management-side service, which mainly provides the following functions:

1. Authenticate the identity of AO.space
2. Provide the registration function of box, user and client
3. Coordinate and manage platform network resources (domain name, communication channel, etc.)
4. SSP switching

> **_NOTE:_** This project uses Quarkus, the Supersonic Subatomic Java Framework. If you want to learn more about Quarkus, please visit its website: [QUARKUS](https://quarkus.io/) .

## Environment Variables

All application configuration properties can be set by the file: application.yml, for more details about how to config them, please refer [Configuration Reference Guide](https://quarkus.io/guides/config-reference). Following are important environment variables that can be changed during the container starting up.

### Data Source

- QUARKUS_DATASOURCE_DB_KIND: used to set the database type. default setting: `mysql`
- QUARKUS_DATASOURCE_USERNAME : used to set the username of database.
- QUARKUS_DATASOURCE_PASSWORD: used to set the password of database.
- QUARKUS_DATASOURCE_JDBC_URL: used to set jdbc url of database. default setting: `jdbc:mysql://127.0.0.1:3306/community`

### Cache

- QUARKUS_REDIS_HOSTS: used to set the connection url of redis. default setting: `redis://localhost:6379`
- QUARKUS_REDIS_PASSWORD: used to set the password of redis.

### Application

- APP_REGISTRY_SUBDOMAIN : used to set the "Root Domain" of box, and it's also part of space endpoint.(You need to add configuration for the root domain name on DNS and Nignx, please refer [AOPlatform-Community Deployment Guide](https://ao.space/en/open/documentation/104002))

For naming conversion rules between name of config and name of environment variables, please refer [The Conversion Rules](https://github.com/eclipse/microprofile-config/blob/master/spec/src/main/asciidoc/configsources.asciidoc#default-configsources). Below are all the default values of above variables that comes from `application.yml` :

```
quarkus:
  datasource:
    db-kind: mysql
    username: root
    password: 123456
    jdbc:
      url: jdbc:mysql://127.0.0.1:3306/community?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=GMT%2B8
  redis:
    hosts: redis://localhost:6379
    password: 123456
app:
  registry:
    subdomain: XXXX # root domain of box
```

## Manually build and run jvm docker image

1. `./mvnw package`
2. `cd /eulixplatform-registry`
3. `docker build --pull -f src/main/docker/Dockerfile.jvm -t platform-registry-jvm-community:latest .`
4. `docker run -itd --name platform-registry -p 8080:8080 -u root -e APP_REGISTRY_SUBDOMAIN="root domain of box" platform-registry-jvm-community:latest`

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_** Quarkus now ships with a Dev UI, which is available in dev mode only at `http://localhost:8080/q/dev/`.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using the following command:

```java script
java -jar target/quarkus-app/quarkus-run.jar
```

## Using OpenAPI and Swagger UI

OpenAPI descriptor and Swagger UI frontend to test your REST endpoints: `http://localhost:8080/platform/q/swagger-ui/`

For more details about the OpenAPI extension, please refer [Using OpenAPI and Swagger UI](https://quarkus.io/guides/openapi-swaggerui).