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

  PKG_VERSION_NOT_EXIST(2002, "pkg version does not exist"),
  PKG_VERSION_EXISTED(2003, "pkg version has already existed"),
  LATEST_APP_VERSION_NOT_EXIST(2010, "latest app version does not exist"),
  LATEST_BOX_VERSION_NOT_EXIST(2011, "latest box version does not exist"),

  PROPOSAL_NOT_EXIST(2004, "proposal does not exist"),
  FILE_NOT_FOUND(2005, "file not found"),
  UPLOAD_FILE_FAILED(2006, "upload file failed"),
  DOWNLOAD_FILE_FAILED(2007, "download file failed"),
  FILE_SIZE_EXCEED_PERMIT(2008, "file exceeds its maximum permitted size of {0} bytes"),
  DIR_CREATE_FAILED(2009, "create directory failed"),

  INPUT_PARAMETER_ERROR(2012, "input parameter:{0} error"),
  QUESTIONNAIRE_NOT_EXIST(2013, "questionnaire does not exist"),
  PAYLOAD_SURVEY_ALREADY_EXIST(2014, "payload survey already exist"),
  PAYLOAD_ANSWER_ALREADY_EXIST(2015, "payload answer already exist"),

  USER_DOMAIN_INVALID(2016, "user domain is invalid"),
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
  DATABASE_ERROR(2027, "database error"),

  NETWORK_CLIENT_NOT_EXIST(2028, "network client does not exist"),
  TIME_PARAMETER_ERROR(2029, "startTime {0} is after endTime {1}"),
  CATALOGUE_NOT_EXIST(2030, "catalogue path does not exist"),
  CATALOGUE_HAS_CREATE(2031, "could not create duplicate catalogue name"),
  CATALOGUE_IS_ROOT(2032, "could not modify root catalogue"),
  ARTICLE_HAS_CREATE(2033, "could not create duplicate article name"),
  ARTICLE_NOT_EXIST(2034, "article does not exist"),
  DELETE_FILE_FAILED(2035, "delete file failed"),
  BOXUUIDS_IS_EMPTY(2036, "boxuuid list is empty, please choose at least one box."),
  RESERVED_DOMAIN_LENGTH_ERROR(2037, "regex length error. Please try others.")
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
