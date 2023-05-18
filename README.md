# Platform Base Service Introduction

- [Brief Introduction](#brief-introduction)
- [System Architecture](#system-architecture)
    - [AO.space Platform](#ao.space-platform)
    - [Base Service](#base-service)
- [Environment Variables](#environment-variables)
- [Build and Run Application](#build-and-run-application)
- [Using OpenAPI and Swagger UI](#using-openapi-and-swagger-ui)
- [Evolution Plan](#evolution-plan)
- [Contribution Guidelines](#contribution-guidelines)
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

## Evolution Plan

- Provide LAN IP direct connection domain name resolution service
- Forwarding proxy service (Platform Proxy Service)
- Java language SDK for basic services on the platform side
- golang language SDK for basic services on the platform side
- Distributed locks based on common middleware such as Mysql and Redis

## Contribution Guidelines

Contributions to this project are very welcome. Here are some guidelines and suggestions to help you get involved in the project.

### Contributing Code

If you want to contribute to the project, the best way is to submit code. Before submitting code, please ensure that you have downloaded and familiarized yourself with the project code repository, and that your code adheres to the following guidelines:

- The code should be as concise as possible, and easy to maintain and expand.
- The code should follow the naming convention agreed by the project to ensure the consistency of the code.
- The code should follow the code style guide of the project, and you can refer to the existing code in the project code library.

If you want to submit code to the project, you can do so by following these steps:

- Fork the project on GitHub.
- Clone your forked project locally.
- Make your modifications and improvements locally.
- Perform tests to ensure that any changes have no impact.
- Commit your changes and create a pull request.

### Code Quality

We attach great importance to the quality of the code, so the code you submit should meet the following requirements:

- Code should be fully tested to ensure its correctness and stability.
- Code should follow good design principles and best practices.
- The code should conform as closely as possible to the relevant requirements of your submitted code contribution.

### Submit Information

Before committing code, please ensure that you provide a meaningful and detailed commit message. This helps us better understand your code contribution and merge it more quickly.

Submission information should include the following:

- Describe the purpose or reason for this code contribution.
- Describe the content or changes of this code contribution.
- (Optional) Describe the test methods or results of this code contribution.

The submission information should be clear and consistent with the submission information agreement of the project code base.

### Problem Reporting

If you encounter problems with the project, or find bugs, please submit an issue report to us. Before submitting an issue report, please ensure that you have thoroughly investigated and experimented with the issue and include as much of the following information as possible:

- Describe the symptoms and manifestations of the problem.
- Describe the scenario and conditions under which the problem occurred.
- Describe contextual information or any relevant background information.
- Information describing your desired behavior.
- (Optional) Provide relevant screenshots or error messages.

Issue reports should be clear and follow the issue reporting conventions of the project's codebase.

### Feature Request

If you want to add new functionality or features to the project, you are welcome to submit a feature request to us. Before submitting a feature request, please make sure you understand the history and current state of the project, and provide as much of the following information as possible:

- Describe the functionality or features you would like to add.
- Describe the purpose and purpose of this function or feature.
- (Optional) Provide relevant implementation ideas or suggestions.

Feature requests should be clear and follow the feature request conventions of the project's codebase.

### Thanks for your contribution

Finally, thank you for your contribution to this project. We welcome contributions in all forms, including but not limited to code contributions, issue reports, feature requests, documentation writing, etc. We believe that with your help, this project will become more perfect and stronger.
