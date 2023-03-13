/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.eulix.platform.common.support.service;

/**
 * Used to define a error raised under the service layer.
 * It consists of following parts:
 * <ol>
 *   <li>code: used to specify the error code. It can be report to top layer for error detecting.</li>
 *   <li>message: used to specify the default error message. It would include
 *   some placeholder used to format a final string result,
 *   and the format uses {@link java.text.MessageFormat}'s style.</li>
 * </ol>
 * You can explicitly define all of service errors here for further using.
 *
 * @see java.text.MessageFormat
 * @see ServiceOperationException
 * @since 1.0.0
 */
public enum ServiceError {
    /**
     * Indicates a unknown error that might be somehow undefined currently.
     */
    UNKNOWN(-1, "unknown error"),
    DATABASE_ERROR(2027, "database error"),
    EXTERNAL_SERVICE_ERROR(2038, "external service:{0} error"),

    PKEY_INVALID(2000, "pkey is invalid"),
    PKEY_EXPIRED(2001, "pkey has expired"),

    DOWNLOAD_FILE_FAILED(2007, "download file failed"),

    INPUT_PARAMETER_ERROR(2012, "input parameter:{0} error"),

    SUBDOMAIN_NOT_EXIST(2017, "subdomain does not exist"),
    SUBDOMAIN_ALREADY_EXIST(2018, "subdomain already exist"),
    SUBDOMAIN_ALREADY_USED(2019, "subdomain already used"),
    SUBDOMAIN_UPPER_LIMIT(2020, "reach subdomain upper limit"),
    BOX_ALREADY_REGISTERED(2021, "box uuid has already registered"),
    BOX_NOT_REGISTERED(2022, "box uuid had not registered"),
    USER_ALREADY_REGISTERED(2023, "user id has already registered"),
    USER_NOT_REGISTERED(2024, "user id has not registered"),
    CLIENT_ALREADY_REGISTERED(2025, "client uuid has already registered"),
    CLIENT_NOT_REGISTERED(2026, "client uuid has not registered"),

    NETWORK_CLIENT_NOT_EXIST(2028, "network client does not exist"),
    BOXUUIDS_IS_EMPTY(2036, "boxuuid list is empty, please choose at least one box."),

    NETWORK_SERVER_NOT_EXIST(2049, "network server does not exist"),
    SUBDOMAIN_NOT_IN_USER(2050, "subdomain is not in use"),
    SUBDOMAIN_IS_RESERVED(2051, "subdomain is reserved"),

    API_JSON_EXCEPTION(2057, "apis Json Processing Exception"),
    API_IO_EXCEPTION(2058, "apis Json Processing Exception"),
    MIGRATION_OUT_ERROR(2059, "there is no domain name for the migration error"),
    MIGRATION_IN_LOCK_ERROR(2060, "migration in acquire lock error"),
    MIGRATION_OUT_LOCK_ERROR(2061, "migration out acquire lock error"),

    SUBDOMAIN_INVALID(2062, "subdomain is invalid"),
    ;

    /**
     * The identity of an error.
     */
    private final int code;

    /**
     * The default message of an error
     */
    private final String message;

    ServiceError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ServiceError valueOf(int code) {
        for (ServiceError e : ServiceError.values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("invalid code for service error - " + code);
    }

    /**
     * Return the code of this error.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the default message of this error.
     */
    public int getCode() {
        return code;
    }
}
