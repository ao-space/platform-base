package xyz.eulix.platform.services.support.service;

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
  private final Object[] messageParameters;

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
