package xyz.eulix.platform.services.support.service;

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
  PKEY_INVALID(2000, "pkey is invalid"),
  PKEY_EXPIRED(2001, "pkey has expired"),

  APP_VERSION_NOT_EXIST(2002, "app version does not exist"),
  APP_VERSION_EXISTED(2003, "app version has already existed"),

  PROPOSAL_NOT_EXIST(2004, "proposal does not exist"),
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
