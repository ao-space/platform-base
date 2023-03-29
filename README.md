# Platform Base Service Introduction

- [Brief Introduction](#brief-introduction)
- [System Architecture](#system-architecture)
    - [AO.space Platform](#ao.space-platform)
    - [Base Service](#base-service)
- [Environment Variables](#environment-variables)
- [Build and Run Application](#build-and-run-application)
- [Using OpenAPI and Swagger UI](#using-openapi-and-swagger-ui)
- [切换中文文档](/README_zn.md)

## Brief Introduction

AO.space is a solution that focuses on protecting personal data security and privacy. Utilizing end-to-end encryption and device-based authentication, users have complete control over their personal accounts and data. AO.space also employs various technologies, including transparent platform forwarding, peer-to-peer acceleration, and LAN direct connection, to enable fast access to personal data from anywhere at any time. Leveraging Progressive Web App and cloud-native technology, AO.space has developed an integrated application ecosystem that could include both front-end and back-end components.

AO.space is composed of three parts: server-side, client-side, and platform. The server-side and client-side run on personal devices and establish encrypted communication channels with public key authentication. The server-side supports x86_64 and aarch64 architectures and can run on personal servers, computers, or other similar devices. The client-side supports Android, iOS, and web platforms, providing users with the convenience of using AO.space anywhere and anytime.

AO.space Platform provides personal devices with transparent communication channel services and secure protection for Internet access. AO.space platform can also be privately deployed. Differing from other solutions, personal account authentication and authorization in AO.space are managed solely by the server running on the personal device. The AO.space platform cannot manage or parse any personal data, and personal account authentication and authorization in AO.space are managed solely by the server-side running on the personal device, ensuring complete control of user data in personal devices.

## System Architecture

![AO.space Platform&BaseService Arch.png](docs/en/asserts/AO.space%20Platform&BaseService%20Arch.png)

### AO.space Platform

The responsibility of AO.space Platform is to establish a transparent communication channel for personal equipment. It includes the Platform Base Service, the Platform Proxy Service, and the Network Transit Server.

- Platform Base Service: provide the registration service of AO.space, and coordinate and manage the platform network resources (domain name, communication channel, etc.).
- Plarform Proxy Service: provide high-availability forwarding and horizontal expansion support for the requests from clients.
- Network Transit Server: provides network support services that penetrate NAT access AO.space through relay forwarding. It is used to forward the requests from clients to AO.space.

> **_Note:_** For a complete deployment guide of AO.space Platform, please refer to [AOPlatform Community Deployment Guide](https://ao.space/en/open/documentation/104002).

### Base Service

Base Service is the implementation of management-side service, which mainly provides the following functions:

1. Authenticate the identity of AO.space
2. Provide the registration function of device, user and client
3. Coordinate and manage platform network resources (domain name, communication channel, etc.)
4. Switch self-hosted AO.space Platform

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

- APP_REGISTRY_SUBDOMAIN : used to set the "Root Domain" of device, and it's also part of space endpoint.(You need to add configuration for the root domain name on DNS and Nignx, please refer [AOPlatform-Community Deployment Guide](https://ao.space/en/open/documentation/104002))

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
    subdomain: XXXX # root domain of device
```

## Build and Run Application

### Manually build and run jvm docker image

1. `./mvnw package`
2. `cd /eulixplatform-registry`
3. `docker build --pull -f src/main/docker/Dockerfile.jvm -t platform-base-jvm-community:latest .`
4. `docker run -itd --name platform-base -p 8080:8080 -u root -e APP_REGISTRY_SUBDOMAIN="root domain of device" platform-base-jvm-community:latest`

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_** Quarkus now ships with a Dev UI, which is available in dev mode only at `http://localhost:8080/q/dev/`.

### Packaging and running the application

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
