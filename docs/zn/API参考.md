# API参考

- [API概览](#API概览)
- [如何调用API](#如何调用API)
- [接口说明](#接口说明)
- [附录](#附录)
    - [状态码](#状态码)
    - [错误码](#错误码)
- [A1. Document Revision History 文档修订记录](#a1.-document-revision-history-文档修订记录)

## API概览

表1 盒子身份认证接口

| 接口 | 接口说明 |
| --------- | ---------------------- |
| [获取访问令牌](#获取访问令牌) | 用于空间平台认证盒子身份，并生成访问令牌 box_reg_key |

表2 注册接口

| 接口 | 接口说明 |
| --------- | ---------------------- |
| [注册盒子](#注册盒子) | 注册傲空间盒子，空间平台为其分配 network client 信息 |
| [删除盒子](#删除盒子) | 删除傲空间盒子注册信息，包含用户注册信息、Client注册信息、网络资源等 |
| [注册用户](#注册用户) | 注册用户，同步注册用户的绑定客户端 |
| [申请用户域名](#申请用户域名) | 申请用户的子域名，子域名全局唯一性 |
| [修改用户域名](#修改用户域名) | 修改用户的子域名，仍然保留用户的历史域名 |
| [删除用户](#删除用户) | 删除用户注册信息，包含Client注册信息等 |
| [注册客户端](#注册客户端) | 注册客户端 |
| [删除客户端](#删除客户端) | 删除客户端注册信息 |

表3 网络资源管理接口

| 接口 | 接口说明 |
| --------- | ---------------------- |
| [查询最新network server信息](#查询最新network server信息) | 用于 network client 查询最新的 network server 信息 |

表4 空间平台切换接口

| 接口 | 接口说明 |
| --------- | ---------------------- |
| [空间平台迁入](#空间平台迁入) | 用于向新空间平台迁入傲空间盒子数据 |
| [空间平台迁出](#空间平台迁出) | 用于旧空间平台进行域名重定向 |

## 如何调用API

本节介绍 REST API 请求的组成，并以“获取访问令牌”为例来说明如何调用 API，该 API 获取盒子的 box_reg_key，box_reg_key 可以用于调用其他 API 时鉴权。

### 请求URI

请求 URI 由如下部分组成： 

{URI-scheme}://{Endpoint}/{resource-path}?{query-string}

尽管请求 URI 包含在请求消息头中，但大多数语言或框架都要求您从请求消息中单独传递它，所以在此单独强调。

表5 URI 中的参数说明

| 参数 | 参数说明 |
| --------- | ---------------------- |
| URI-scheme | 表示用于传输请求的协议，如HTTP、HTTPS。HTTPS表示通过安全的HTTPS访问该资源 |
| Endpoint | 指定承载 REST 服务端点的服务器域名或 IP |
| resource-path | 资源路径，即API访问路径。从具体API的URI模块获取，例如“获取访问令牌” API 的 resource-path 为 “/v2/platform/auth/box_reg_keys” |
| query-string | 查询参数，是可选部分，并不是每个API都有查询参数。查询参数前面需要带一个“?”，形式为“参数名=参数取值”，例如“?limit=10”，表示查询不超过10条数据 |

### 请求消息头

附加请求头字段，如指定的URI和HTTP方法所要求的字段。例如定义消息体类型的请求头“Content-Type”，请求鉴权信息等。 详细的公共请求消息头字段请参见表6。

表6 公共请求消息头

| 名称 | 是否必选 | 描述 | 示例 |
| --- | ------- | ------- | --- |
| Content-Type | 否 | 消息体的类型（格式）。推荐用户使用默认值application/json，有其他取值时会在具体接口中专门说明 | application/json |
| Request-Id | 是 | 请求的 request id，用于追踪请求的执行情况 | e9993fc787d94b6c886cbaa340f9c0f4 |
| Box-Reg-Key | 否，使用盒子身份认证时该字段必选 | Box-Reg-Key也就是调用“获取访问令牌” 接口的响应值，该接口是少数不需要认证的接口。 | brk_YVj29IJAD3 |

您可以使用curl、Postman或直接编写代码等方式发送请求调用API。

### 返回结果

请求发送以后，您会收到响应，其中包含状态码、响应消息头和消息体。

状态码是一组从 1xx 到 5xx 的数字代码，状态码表示了请求响应的状态，完整的状态码列表请参见 [状态码](#状态码)。

响应消息体通常以结构化格式（如JSON或XML）返回，与响应消息头中Content-Type对应，传递除响应消息头之外的内容。

接口执行成功返回的响应消息体详见 [接口说明](#接口说明)。当接口调用出错时，会返回错误码及错误信息说明，错误响应的Body体格式如下所示。

```错误响应的Body体
{
    "error": "SSP-2012",
    "message": "input parameter:{0} error"
}
```

其中，error 表示错误码，message 表示错误描述信息。

## 接口说明

### 获取访问令牌

功能介绍
- 用于空间平台认证盒子身份，并生成访问令牌 box_reg_key

URI
- POST /v2/platform/auth/box_reg_keys

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 仅需要 Request-Id 参数。

#### 请求参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| boxUUID | 是 | String | 盒子的 UUID |
| serviceIds | 是 | String | 平台id：空间平台（serviceId=10001） |
| sign | 否 | String | 签名，使用公钥验证盒子身份时必传 |

#### 响应参数

状态码：200

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| boxUUID | String | 盒子的 UUID |
| tokenResults | Array of [TokenResult](#TokenResult) | 盒子的访问令牌 |

#### TokenResult

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| serviceId | String | 平台id |
| boxRegKey | String | 盒子的访问令牌 |
| expiresAt | OffsetDateTime | 令牌有效时间 |

### 注册盒子

功能介绍
- 注册傲空间盒子，空间平台为其分配 network client 信息

URI
- POST /v2/platform/boxes

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 需要 Request-Id、Box-Reg-key 参数。

#### 请求参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| boxUUID | 是 | String | 盒子的 UUID |

#### 响应参数

状态码：200

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| boxUUID | String | 盒子的 UUID |
| networkClient | [NetworkClient](#NetworkClient) | 为其分配 network client 信息 |

#### NetworkClient

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| clientId | String | network 的客户端 ID |
| secretKey | String | 访问密钥 |

### 删除盒子

功能介绍
- 删除傲空间盒子注册信息，包含用户注册信息、Client注册信息、网络资源等

URI
- DELETE /v2/platform/boxes/{box_uuid}

Path参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| box_uuid | 是 | String | 盒子的 UUID |

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 需要 Request-Id、Box-Reg-key 参数。

#### 请求参数

无

#### 响应参数

状态码：204

### 注册用户

功能介绍
- 注册用户，同步注册用户的绑定客户端

URI
- POST /v2/platform/boxes/{box_uuid}/users

Path参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| box_uuid | 是 | String | 盒子的 UUID |

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 需要 Request-Id、Box-Reg-key 参数。

#### 请求参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| userId | 是 | String | 用户的 ID |
| subdomain | 是 | String | 用户被指定的子域名 |
| userType | 是 | String | 用户类型（管理员、普通成员），取值：user_admin、user_member |
| clientUUID | 是 | String | 客户端的 UUID |

#### 响应参数

状态码：200

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| boxUUID | String | 盒子的 UUID |
| userId | String | 用户的 ID |
| userDomain | String | 为用户分配的用户域名，该域名可以用于后续的业务访问 |
| userType | String | 用户类型（管理员、普通成员） |
| clientUUID | String | 客户端的 UUID |

### 申请用户域名

功能介绍
- 申请用户的子域名，子域名全局唯一性

URI
- POST /v2/platform/boxes/{box_uuid}/subdomains

Path参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| box_uuid | 是 | String | 盒子的 UUID |

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 需要 Request-Id、Box-Reg-key 参数。

#### 请求参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| effectiveTime | 是 | String | 有效期，单位秒，最长7天 |

#### 响应参数

状态码：200

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| boxUUID | String | 盒子的 UUID |
| subdomain | String | 用户被指定的子域名 |
| expiresAt | OffsetDateTime | 有效期 |

### 修改用户域名

功能介绍
- 修改用户的子域名，仍然保留用户的历史域名

URI
- PUT /v2/platform/boxes/{box_uuid}/users/{user_id}/subdomain

Path参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| box_uuid | 是 | String | 盒子的 UUID |
| user_id | 是 | String | 用户的 ID |

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 需要 Request-Id、Box-Reg-key 参数。

#### 请求参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| subdomain | 是 | String | 用户指定的新的子域名 |

#### 响应参数

状态码：200

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| success | Boolean | 是否成功 |
| boxUUID | String | 盒子的 UUID, success 为 true 时返回 |
| userId | String | 用户的 ID, success 为 true 时返回 |
| subdomain | String | 用户指定的新的子域名, success 为 true 时返回 |
| code | String | 错误码, success 为 false 时返回 |
| error | String | 错误消息, success 为 false 时返回 |
| recommends | Array of String | 推荐的subdomain, success 为 false 时返回 |

### 删除用户

功能介绍
- 删除用户注册信息，包含Client注册信息等

URI
- DELETE /v2/platform/boxes/{box_uuid}/users/{user_id}

Path参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| box_uuid | 是 | String | 盒子的 UUID |
| user_id | 是 | String | 用户的 ID |

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 需要 Request-Id、Box-Reg-key 参数。

#### 请求参数

无

#### 响应参数

状态码：204

### 注册客户端

功能介绍
- 注册客户端

URI
- POST /v2/platform/boxes/{box_uuid}/users/{user_id}/clients

Path参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| box_uuid | 是 | String | 盒子的 UUID |
| user_id | 是 | String | 用户的 ID |

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 需要 Request-Id、Box-Reg-key 参数。

#### 请求参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| clientUUID | 是 | String | 客户端的 UUID |
| clientType | 是 | String | 客户端类型（绑定、扫码授权），取值：client_bind、client_auth |

#### 响应参数

状态码：200

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| boxUUID | String | 盒子的 UUID |
| userId | String | 用户的 ID |
| clientUUID | String | 客户端的 UUID |
| clientType | String | 客户端类型（绑定、扫码授权） |

### 删除客户端

功能介绍
- 删除客户端注册信息

URI
- DELETE /v2/platform/boxes/{box_uuid}/users/{user_id}/clients/{client_uuid}

Path参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| box_uuid | 是 | String | 盒子的 UUID |
| user_id | 是 | String | 用户的 ID |
| client_uuid | 是 | String | 客户端的 UUID |

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 需要 Request-Id、Box-Reg-key 参数。

#### 请求参数

无

#### 响应参数

状态码：204

### 查询最新network server信息

功能介绍
- 用于 network client 查询最新的 network server 信息

URI
- GET /v2/platform/servers/network/detail

Query参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| network_client_id | 是 | String | network 的客户端 ID |

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 仅需要 Request-Id 参数。

#### 请求参数

无

#### 响应参数

状态码：200

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| serverAddress | String | network 服务器地址 |

### 空间平台迁入

功能介绍
- 用于向新空间平台迁入傲空间盒子数据

URI
- POST /v2/platform/boxes/{box_uuid}/migration

Path参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| box_uuid | 是 | String | 盒子的 UUID |

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 需要 Request-Id、Box-Reg-key 参数。

#### 请求参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| networkClientId | 是 | String | network 的客户端 ID |
| userInfos | 是 | Array of [UserMigrationInfo](#UserMigrationInfo) | 用户列表 |

#### UserMigrationInfo

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| userId | 是 | String | 用户的 ID |
| userDomain | 是 | String | 用户域名 |
| userType | 是 | String | 用户类型（管理员、普通成员） |
| clientInfos | 是 | Array of [ClientMigrationInfo](#ClientMigrationInfo) | Client 列表 |

#### ClientMigrationInfo

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| clientUUID | 是 | String | 客户端的 UUID |
| clientType | 是 | String | 客户端类型（绑定、扫码授权），取值：client_bind、client_auth |

#### 响应参数

状态码：200

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| boxUUID | String | 盒子的 UUID |
| networkClient | [NetworkClient](#NetworkClient) | 为盒子分配的 network client 信息 |
| userInfos | Array of [UserMigrationInfo](#UserMigrationInfo) | 用户列表 |

### 空间平台迁出

功能介绍
- 用于旧空间平台进行域名重定向

URI
- POST /v2/platform/boxes/{box_uuid}/route

Path参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| box_uuid | 是 | String | 盒子的 UUID |

#### 请求消息头

该操作消息头与普通请求一样，请参见“表6 公共请求消息头”。该 API 需要 Request-Id、Box-Reg-key 参数。

#### 请求参数

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| userDomainRouteInfos | 是 | Array of [UserDomainRouteInfo](#UserDomainRouteInfo) | 用户域名映射关系 |

#### UserDomainRouteInfo

| 参数 | 是否必选 | 参数类型 | 说明 |
| --- | ------- | ------- | --- |
| userId | 是 | String | 用户的 ID |
| userDomainRedirect | 是 | String | 重定向的用户域名 |

#### 响应参数

状态码：200

| 参数 | 参数类型 | 说明 |
| --- | ------- | --- |
| boxUUID | String | 盒子的 UUID |
| userDomainRouteInfos | [UserDomainRouteInfo](#UserDomainRouteInfo) | 用户域名映射关系 |

## 附录

### 状态码

| 正常状态码 | 说明 |
| -------- | --- |
| 200 | OK |
| 201 | Created |
| 202 | Accepted |
| 204 | No Content |

| 错误状态码 | 说明 |
| -------- | --- |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 405 | Method Not Allowed |
| 413 | Request Entity Too Large |
| 415 | Unsupported Media Type |
| 500 | Internal Server Error |
| 503 | Service Unavailable |

### 错误码

| 状态码 | 错误码 | 错误信息 | 说明 |
| ----- | ----- | ------ | ---- |
| 400 | SSP-2012 | input parameter:{0} error | 请求参数错误 |
| 400 | SSP-2017 | subdomain does not exist | 子域名不存在 |
| 400 | SSP-2018 | subdomain already exist | 子域名已存在 |
| 400 | SSP-2019 | subdomain already used | 子域名已使用 |
| 400 | SSP-2020 | reach subdomain upper limit | 子域名数量已达到上限 |
| 400 | SSP-2021 | box uuid has already registered | box uuid 已注册 |
| 400 | SSP-2022 | box uuid had not registered | box uuid 未注册 |
| 400 | SSP-2023 | user id has already registered | user id 已注册 |
| 400 | SSP-2024 | user id has not registered | user id 未注册 |
| 400 | SSP-2025 | client uuid has already registered | client uuid 已注册 |
| 400 | SSP-2026 | client uuid has not registered | client uuid 未注册 |
| 400 | SSP-2028 | network client does not exist | network client 不存在 |
| 400 | SSP-2049 | network server does not exist | network server 不存在 |
| 400 | SSP-2050 | subdomain is not in use | 子域名未使用 |
| 400 | SSP-2051 | subdomain is reserved | 子域名不合法 |
| 400 | SSP-2060 | migration in acquire lock error | 迁入操作获取锁失败 |
| 400 | SSP-2061 | migration out acquire lock error | 迁出操作获取锁失败 |

## A1. Document Revision History 文档修订记录

- 2023/03/21：API参考