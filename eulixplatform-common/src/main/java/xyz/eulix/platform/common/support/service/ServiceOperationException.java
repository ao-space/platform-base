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

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Runtime exception for service layer reporting specific operation error.
 * <p>
 * This exception can only be used on the surface of service public method.
 * It consists of a error code and a message which would be translate to a
 * locale message. And the error can be a value of {@linkplain ServiceError}.
 *
 * @see ServiceError
 * @since 1.0.0
 */
public class ServiceOperationException extends RuntimeException {

  private final int errorCode;
  private final transient Object[] messageParameters;

  public ServiceOperationException(Throwable cause, ServiceError error, Object... parameters) {
    super(Objects.requireNonNull(error).getMessage(), Objects.requireNonNull(cause));
    this.errorCode = error.getCode();
    this.messageParameters = parameters;
  }

  public ServiceOperationException(ServiceError error, Object... parameters) {
    super(error.getMessage());
    this.errorCode = error.getCode();
    this.messageParameters = parameters;
  }

  public ServiceOperationException(Throwable cause, int errorCode, String message, Object... parameters) {
    super(Objects.requireNonNull(message), Objects.requireNonNull(cause));
    this.errorCode = errorCode;
    this.messageParameters = parameters;
  }

  public ServiceOperationException(int errorCode, String message, Object... parameters) {
    super(Objects.requireNonNull(message));
    this.errorCode = errorCode;
    this.messageParameters = parameters;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getMessage() {
    return MessageFormat.format(super.getMessage(), getMessageParameters());
  }

  public Object[] getMessageParameters() {
    return messageParameters;
  }
}
