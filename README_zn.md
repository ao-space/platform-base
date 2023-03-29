# Platform Base Service 介绍

- [简介](#简介)
- [系统架构](#系统架构)
    - [傲空间平台](#傲空间平台)
    - [Base Service](#base-service)
- [环境变量](#环境变量)
- [构建和运行应用程序](#构建和运行应用程序)
- [使用 OpenAPI 和 Swagger UI](#使用-openapi-和-swagger-ui)
- [English Documents](/README.md)

## 简介

AO.space（傲空间）是一个以保护个人数据安全和隐私为核心的解决方案。通过端对端加密、基于设备认证等技术，确保用户完全掌控个人账号和数据。同时，采用平台透明转发、点对点加速、局域网直连等技术，让用户随时随地的极速访问个人数据。傲空间利用 PWA（Progressive Web App）和云原生技术，设计并打造前后端一体的应用生态。

AO.space（傲空间）由服务端、客户端、平台三个部分组成。服务端和客户端只运行在个人设备上，使用公钥认证建立加密通信通道。服务端是傲空间管理保护用户数据的核心部分，目前支持 x86_64 和 aarch64 两个架构，可运行在个人服务器、个人计算机等设备上。客户端让用户在不同平台上快速安全的访问个人数据，目前支持多个平台，包括 Android、iOS 和 Web ，方便用户随时随地使用。

AO.space Platform（即傲空间平台），则为个人设备提供透明通信通道服务和互联网访问的安全防护，并且可以进行私有部署。与其他解决方案中的平台不同，傲空间下个人账号的认证和鉴权只由运行在个人设备的服务端管理，傲空间平台无法管理和解析个人的任何数据，实现用户的个人数据完全由用户掌控在个人设备上。

## 系统架构

![傲空间平台&Platform Base架构.png](docs/zn/asserts/傲空间平台&Platform%20Base架构.png)

### 傲空间平台

傲空间平台的职责是为个人设备建立透明的通信信道。包含基础服务（Platform Base Service）、转发代理服务（Plarform Proxy Service）、中继转发服务器（Network Transit Server）。

- 基础服务（Platform Base Service）：为傲空间设备提供注册服务，以及协调和管理平台网络资源（域名，Network Server通信信道等）。
- 转发代理服务（Plarform Proxy）：为傲空间用户域名流量提供高可用转发和横向扩容的支持。
- 中继转发服务器（Network Transit Server）提供通过中继转发的方式穿透 NAT 访问设备的网络支持服务。将来自 Clients 的流量转发至傲空间设备。

> **_注意：_** 完整的傲空间平台部署指南，请参阅 [AOPlatform社区部署指南](https://ao.space/open/documentation/104002) 。

### Base Service

Base Service 是傲空间平台管理面的实现，主要提供以下功能：

1. 认证傲空间设备身份
2. 提供傲空间设备、用户、客户端注册功能
3. 协调和管理平台网络资源（域名，Network Transit Server通信信道等）
4. 傲空间平台切换

> **_注意：_** 项目使用了 Quarkus，它是一个 Red Hat 公司开源的云原生 Java 框架。如果您想了解有关Quarkus的更多信息，请访问其网站：[QUARKUS](https://quarkus.io/) 。

## 环境变量

所有应用程序配置都可以通过配置文件 “application.yml“ 进行设置，有关如何配置它们的详细信息，请参阅 [配置参考指南](https://quarkus.io/guides/config-reference )。以下是在容器启动期间可以更改的重要环境变量。

### 数据源
- QUARKUS_DATASOURCE_DB_KIND：用于设置数据库类型。默认设置：`mysql`
- QUARKUS_DATASOURCE_USERNAME：用于设置数据库的用户名。
- QUARKUS_DATASOURCE_PASSWORD：用于设置数据库的密码。
- QUARKUS_DATASOURCE_JDBC_URL：用于设置数据库的 jdbc url。默认设置：`jdbc:mysql://127.0.0.1:3306/community`

### 缓存
- QUARKUS_REDIS_HOSTS：用于设置 redis 的连接url。默认设置：`redis://localhost:6379`
- QUARKUS_REDIS_PASSWORD：用于设置 redis 的密码。

### 应用程序
- APP_REGISTRY_SUBDOMAIN：用于设置傲空间设备的“根域名”，也是傲空间用户域名的一部分。您需要在 DNS 和 Nignx 上配置根域名，请参阅 [AOPlatform社区部署指南](https://ao.space/open/documentation/104002)

有关配置名称和环境变量名称之间的命名转换规则，请参阅 [转换规则](https://github.com/eclipse/microprofile-config/blob/master/spec/src/main/asciidoc/configsources.asciidoc#default-configsources )。以下是来自 “application.yml” 的上述变量的所有默认值：

```默认配置
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
    subdomain: XXXX # 傲空设备的根域名
```

## 构建和运行应用程序

### 手动构建和运行 jvm docker 镜像

1. `./mvnw package`
2. `cd /eulixplatform-registry`
3. `docker build --pull -f src/main/docker/Dockerfile.jvm -t platform-base-jvm-community:latest .`
4. `docker run -itd --name platform-base -p 8080:8080 -u root -e APP_REGISTRY_SUBDOMAIN="傲空间设备的根域名" platform-base-jvm-community:latest`

### 在开发模式下运行应用程序

您可以在开发模式下运行应用程序，该模式支持实时编码，使用：

```mvnw命令
./mvnw compile quarkus:dev
```

> **_注意：_** Quarkus现在附带一个开发UI，该UI仅在开发模式下可用：`http://localhost:8080/q/dev/`

### 打包并运行应用程序

应用程序可以使用以下方式打包：

```mvnw命令
./mvnw package
```

它会在 “target/quarkus-app/” 目录中生成 “quarkus-run.jar” 文件。 请注意，它不是一个 _über-jar_ ，因为依赖项被复制到“target/quarkus-app/lib/”目录中。 如果要构建 _über-jar_ ，请执行以下命令：

```mvnw命令
./mvnw package -Dquarkus.package.type=uber-jar
```

该应用程序现在可以使用如下命令运行：

```java命令
java -jar target/quarkus-app/quarkus-run.jar
```

## 使用 OpenAPI 和 Swagger UI

OpenAPI 描述符和 Swagger UI 前端来测试 REST 端点，访问地址：`http://localhost:8080/platform/q/swagger-ui/`

有关OpenAPI扩展的更多详细信息，请参阅 [使用OpenAPI和Swagger UI](https://quarkus.io/guides/openapi-swaggerui)
