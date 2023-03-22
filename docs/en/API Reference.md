#API Reference

- [API Overview](#api-overview)
- [How to Call API](#how-to-call-api)
- [Interface Description](#interface-description)
- [Appendix](#appendix)
    - [Status Code](#status-code)
    - [Error Code](#error-code)
- [A1. Document Revision History](#a1-document-revision-history)

## API Overview

Table 1 Box Identity Authentication Interface

| Interface | Interface Description |
| --------- | ---------------------- |
| [Obtain Box_Reg_Key](#obtain-box_reg_key) | Used to authenticate the identity of the box on the space platform and generate box_reg_keys |

Table 2 Registration Interface

| Interface | Interface Description |
| --------- | ---------------------- |
| [Register Box](#register-box) | Register the AO Space Box, and the space platform assigns network client information to it |
| [Delete Box](#delete-box) | Delete AO Space Box registration information, including user registration information, client registration information, network resources, etc |
| [Register User](#register-user) | Register User, including the binding client of the registered user |
| [Generate User Domain Name](#generate-user-domain-name) | Generate the user's subdomain name, and the subdomain name is unique globally |
| [Modify User Domain Name](#modify-user-domain-name) | Modify the user's subdomain name, still retaining the user's historical subdomain name |
| [Delete User](#delete-user) | Delete user registration information, including client registration information, etc |
| [Register Client](#register-client) | Register Client |
| [Delete Client](#delete-client) | Delete client registration information |

Table 3 Network Resource Management Interface

| Interface | Interface Description |
| --------- | ---------------------- |
|[Query Network Server Information](#query-network-server-information) | Used by network clients to query the latest network server information |

Table 4 Space Platform Switching Interface

| Interface | Interface Description |
| --------- | ---------------------- |
| [Space Platform Migration](#space-platform-migration) | Used to migrate AO Space Box data to the new space platform |
| [Space Platform Migration Out](#space-platform-migration-out) | Used for domain name redirection on old space platform |

## How to call API

This section describes the composition of REST API requests, and takes "Obtain Box_Reg_Key" as an example to illustrate how to call the API, which obtains the box of the box_reg_key，box_reg_key can be used to authenticate when calling other APIs.

### Request URI

The request URI consists of the following parts:

{URI-scheme}://{Endpoint}/{resource-path}? {query-string}

Although the request URI is included in the request header, most languages or frameworks require you to pass it separately from the request message, so it is emphasized separately here.

Table 5 Parameter Description in URI

|Parameter | Parameter Description|
| --------- | ---------------------- |
| URI-scheme | Indicates the protocol used to transmit requests, such as HTTP and HTTPS. HTTPS means accessing the resource through secure HTTPS |
| Endpoint | Specifies the domain name or IP address of the server hosting the REST service endpoint |
| resource-path | Resource path, which is the API access path. Obtained from the URI module of a specific API, for example, the resource path of the "Get Access Token" API is "/v2/platform/auth/box_reg_keys" |
| query-string | Query parameters, which are optional, and not every API has query parameters. Query parameters need to be preceded by a "?" in the form of "parameter name=parameter value", such as "? Limit=10", indicating that the query does not exceed 10 pieces of data |

### Request Message Header

Additional request header fields, such as those required by the specified URI and HTTP method. For example, define the request header "Content Type" of the message body type, request authentication information, and so on. See Table 6 for detailed public request header fields.

Table 6 Public Request Message Headers

| Name | Required | Description | Example|
| --- | ------- | ------- | --- |
| Content-Type | No | Type (format) of the message body. It is recommended that users use the default value application/json. If there are other values, they will be specifically described in the specific interface | application/json |
| Request-Id | Yes | The request id of the request, used to track the execution of the request | e9993fc787d94b6c886cbaa340f9c0f4 |
| Box-Reg-Key| No, this field is required when using box authentication | Box_Reg_Key is the response value for calling the "Obtain Box_Reg_Key" interface, which is a few interfaces that do not require authentication| brk_ YVj29IJAD3 |

You can use curl, Postman, or directly write code to send requests to invoke the API.

### Return Results

After the request is sent, you will receive a response that contains the status code, response message header, and message body.

The status code is a set of numeric codes ranging from 1xx to 5xx. The status code represents the status of the request response. For a complete list of status codes, see [Status Code](#Status Code).

The response message body is usually returned in a structured format (such as JSON or XML), corresponding to the Content Type in the response message header, and passing content other than the response message header.

The body of the response message returned after successful interface execution is detailed in [Interface Description](#Interface Description). When an interface call fails, an error code and error message description will be returned. The body format of the error response is shown below.

```error response
{
    "error": "SSP-2012",
    "message": "input parameter:{0} error"
}
```

Where error represents the error code and message represents the error description information.

## Interface Description

### Obtain Box_Reg_Key

Function Introduction
- Used to authenticate the identity of the box on the space platform and generate box_reg_keys.

URI
- POST /v2/platform/auth/box_reg_keys

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API only requires the Request-Id parameter.

#### Request Parameters

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| boxUUID | Yes | String | UUID of the box |
| serviceIds | Yes | String | Platform id: Space Platform (serviceId=10001) |
| sign | No | String | Signature, required when using the public key to verify the identity of the box |

#### Response parameters

Status code: 200

|Parameter | Parameter Type | Description |
| --- | ------- | --- |
| boxUUID | String | The UUID of the box |
| tokenResults | Array of [TokenResult](#TokenResult) | The access token for the box |

#### TokenResult

| Parameter | Parameter Type | Description |
| --- | ------- | --- |
| serviceId | String | Platform ID |
| boxRegKey | String | The access token of the box |
| expiresAt | OffsetDateTime | Token validity time |

### Register Box

Function Introduction
- Register AO Space Box, and the space platform assigns network client information to it

URI
- POST /v2/platform/boxes

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API requires the Request-Id and Box-Reg-key parameters.

#### Request Parameters

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| boxUUID | Yes | String | UUID of the box |

#### Response parameters

Status code: 200

| Parameter | Parameter Type | Description |
| --- | ------- | --- |
| boxUUID | String | The UUID of the box |
| networkClient | [NetworkClient](#NetworkClient) | Assign network client information to it |

#### NetworkClient

| Parameter | Parameter Type | Description |
| --- | ------- | --- |
| clientId | String | The client ID of the network |
| secretKey | String | Access key |

### Delete Box

Function Introduction
- Delete the registration information of AO Space Box, including user registration information, client registration information, network resources, etc

URI
- DELETE /v2/platform/boxes/{box_uuid}

Path parameter

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| box_uuid | Yes | String | UUID of the box |

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API requires the Request-Id and Box-Reg-key parameters.

#### Request Parameters

--

#### Response parameters

Status code: 204

### Register User

Function Introduction
- Register users and synchronize their binding clients

URI
- POST /v2/platform/boxes/{box_uuid}/users

Path parameter

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| box_uuid | Yes | String | UUID of the box |

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API requires the Request-Id and Box-Reg-key parameters.

#### Request Parameters

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| userId | Yes | String | User ID |
| subdomain | Yes | String | The subdomain name specified by the user |
| userType | Yes | String | User type (administrator, member), value: user_admin、user_member |
| clientUUID | Yes | String | The UUID of the client |

#### Response parameters

Status code: 200

| Parameter | Parameter Type | Description |
| --- | ------- | --- |
| boxUUID | String | The UUID of the box |
| userId | String | The ID of the user |
| userDomain | String | The user domain name assigned to the user, which can be used for subsequent box access |
| userType | String | User type (administrator, member) |
| clientUUID | String | The UUID of the client |

### Generate User Domain Name

Function Introduction
- Generate the user's subdomain name, and the subdomain name is unique globally

URI
- POST /v2/platform/boxes/{box_uuid}/subdomains

Path parameter

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| box_uuid | Yes | String | UUID of the box |

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API requires the Request-Id and Box-Reg-key parameters.

#### Request Parameters

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| effectiveTime | Yes | String | Validity period, in seconds, up to 7 days |

#### Response parameters

Status code: 200

| Parameter | Parameter Type | Description |
| --- | ------- | --- |
| boxUUID | String | The UUID of the box |
| subdomain | String | The specified subdomain name of the user |
| expiresAt | OffsetDateTime | Validity |

### Modify User Domain Name

Function Introduction
- Modify the user's subdomain name, still retaining the user's historical subdomain name

URI
- PUT /v2/platform/boxes/{box_uuid}/users/{user_id}/subdomain

Path parameter

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| box_uuid | Yes | String | UUID of the box |
| user_id | Yes | String | User ID |

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API requires the Request-Id and Box-Reg-key parameters.

#### Request Parameters

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| subdomain | Yes | String | The new subdomain name specified by the user |

#### Response parameters

Status code: 200

| Parameter | Parameter Type | Description |
| --- | ------- | --- |
| success | Boolean | Whether successful or not |
| boxUUID | String | The UUID of the box, returned when success is true |
| userId | String | The ID of the user. Returned when success is true |
| subdomain | String | The new subdomain name specified by the user. Returns when success is true |
| code | String | Error code, returned when success is false |
| error | String | Error message, returned when success is false |
| recommendations | Array of String | Recommended subdomains, returned when success is false |

### Delete User

Function Introduction
- Delete user registration information, including client registration information, etc

URI
- DELETE /v2/platform/boxes/{box_uuid}/users/{user_id}

Path parameter

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| box_uuid | Yes | String | UUID of the box |
| user_id | Yes | String | User ID |

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API requires the Request-Id and Box-Reg-key parameters.

#### Request Parameters

--

####Response parameters

Status code: 204

### Register Client

Function Introduction
- Register Client

URI
- POST /v2/platform/boxes/{box_uuid}/users/{user_id}/clients

Path parameter

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| box_uuid | Yes | String | UUID of the box |
| user_id | Yes | String | User ID |

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API requires the Request-Id and Box-Reg-key parameters.

#### Request Parameters

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| clientUUID | Yes | String | The UUID of the client |
| clientType | Yes | String | Client type (binding, scanning authorization), value: client_bind、client_auth |

#### Response parameters

Status code: 200

| Parameter | Parameter Type | Description |
| --- | ------- | --- |
| boxUUID | String | The UUID of the box |
| userId | String | The ID of the user |
| clientUUID | String | The UUID of the client |
| clientType | String | Client type (binding, scanning authorization) |

### Delete Client

Function Introduction
- Delete client registration information

URI
- DELETE /v2/platform/boxes/{box_uuid}/users/{user_id}/clients/{client_uuid}

Path parameter

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| box_uuid | Yes | String | UUID of the box |
| user_id | Yes | String | User ID |
| client_uuid | Yes | String | UUID of the client |

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API requires the Request-Id and Box-Reg-key parameters.

#### Request Parameters

--

#### Response parameters

Status code: 204

### Query Network Server Information

Function Introduction
- Used by network clients to query the latest network server information

URI
- GET /v2/platform/servers/network/detail

Query Parameters

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| network_client_Id | Yes | String | The client ID of the network |

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API only requires the Request Id parameter.

#### Request Parameters

--

#### Response parameters

Status code: 200

| Parameter | Parameter Type | Description |
| --- | ------- | --- |
| serverAddress | String | network server address |

### Space Platform Migration

Function Introduction
- Used to migrate Ao Space Box data to the new space platform

URI
- POST /v2/platform/boxes/{box_uuid}/migration

Path parameter

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| box_Uuid | Yes | String | UUID of the box |

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API requires the Request-Id and Box-Reg-key parameters.

#### Request Parameters

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| networkClientId | Yes | String | The client ID of the network |
| userInfos | Yes | Array of [UserMigrationInfo](#UserMigrationInfo) | User list |

#### UserMigrationInfo

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| userId | Yes | String | User ID |
| userDomain | Yes | String | User domain name |
| userType | Yes | String | User type (administrator, member) |
| clientInfos | Yes | Array of [ClientMigrationInfo](#ClientMigrationInfo) | Client List |

#### ClientMigrationInfo

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| clientUUID | Yes | String | The UUID of the client |
| clientType | Yes | String | Client type (binding, scanning authorization), value: client_bind、client_auth |

#### Response parameters

Status code: 200

| Parameter | Parameter Type | Description |
| --- | ------- | --- |
| boxUUID | String | The UUID of the box |
| networkClient | [NetworkClient](#NetworkClient) | Network client information assigned to the box |
| userInfos | Array of [UserMigrationInfo](#UserMigrationInfo) | User list |

### Space Platform Migration Out

Function Introduction
- Used for domain name redirection on old space platforms

URI
- POST /v2/platform/boxes/{box_uuid}/route

Path parameter

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| box_uuid | Yes | String | UUID of the box |

#### Request Message Header

This operation message header is the same as a normal request. Please refer to "Table 6 Public Request Message Headers". The API requires the Request-Id and Box-Reg-key parameters.

#### Request Parameters

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| userDomainRouteInfos | Yes | Array of [UserDomainRouteInfo](#UserDomainRouteInfo) | User domain name mapping relationship |

#### UserDomainRouteInfo

| Parameter | Required | Parameter Type | Description |
| --- | ------- | ------- | --- |
| userId | Yes | String | User ID |
| userDomainRedirect | Yes | String | Redirected user domain name |

#### Response parameters

Status code: 200

| Parameter | Parameter Type | Description |
| --- | ------- | --- |
| boxUUID | String | The UUID of the box |
| userDomainRouteInfos | [UserDomainRouteInfo](#UserDomainRouteInfo) | User domain name mapping relationship |

## Appendix

### Status Code

|Normal status code | Description|
| -------- | --- |
| 200 | OK |
| 201 | Created |
| 202 | Accepted |
| 204 | No Content |

|Error status code | Description|
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

### Error Code

| Status Code | Error Code | Error Message | Description |
| ----- | ----- | ------ | ---- |
| 400 | SSP-2012 | input parameter: {0} error | Request parameter error |
| 400 | SSP-2017 | subdomain does not exist | The subdomain name does not exist |
| 400 | SSP-2018 | Subdomain already exists | Subdomain already exists |
| 400 | SSP-2019 | Subdomain already used | Subdomain already used |
| 400 | SSP-2020 | reach subdomain upper limit | The number of subdomains has reached the upper limit |
| 400 | SSP-2021 | box uuid has already registered | Box uuid has already registered |
| 400 | SSP-2022 | box uuid had not registered | Box uuid not registered |
| 400 | SSP-2023 | user id has already registered | User id has already registered |
| 400 | SSP-2024 | user id has not registered | User id is not registered |
| 400 | SSP-2025 | client uuid has already registered | Client uuid has already registered |
| 400 | SSP-2026 | client uuid has not registered | Client uuid is not registered |
| 400 | SSP-2028 | network client does not exist | Network client does not exist |
| 400 | SSP-2049 | network server does not exist | Network server does not exist |
| 400 | SSP-2050 | Subdomain is not in use | Subdomain is not in use |
| 400 | SSP-2051 | subdomain is reserved | The subdomain name is illegal |
| 400 | SSP-2060 | migration in acquire lock error | Migration in acquire lock failed |
| 400 | SSP-2061 | migration out acquire lock error | The migration out operation failed to acquire the lock |

## A1. Document Revision History

- 2023/03/21: API Reference