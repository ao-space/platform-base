package xyz.eulix.platform.common.support;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.jboss.logging.Logger;
import xyz.eulix.platform.common.support.service.ServiceError;
import xyz.eulix.platform.common.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;

/**
 * It provides application scoped {@code Mappers}, {@code Filters} etc. configurations
 * for all of REST requests and responses, such as mapping error result or filtering &
 * dumping request or response access log. You can also add additional configurations
 * in this class.
 *
 * @since 1.0.0
 * @author Haibo Luo
 */
@ApplicationScoped
public class RestConfiguration {

  static final Logger LOG = Logger.getLogger("rest.log");

  public static final String REQUEST_ID = "Request-Id";

  /**
   * It provides exception result mapping for REST responses. For more information:
   * <a href="https://developer.jboss.org/docs/DOC-48310">https://developer.jboss.org/docs/DOC-48310</a>.
   */
  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Context
    HttpServerRequest request;

    @Override
    public Response toResponse(Exception exception) {
      return convertToErrorResponse(request, exception);
    }
  }

  /**
   * 提供专门针对 ConstraintViolationException 异常的拦截，因为默认通过 {@link ErrorMapper} 无法拦截到这类异常。
   * 具体参考：https://stackoverflow.com/questions/34452996/jaxrs-jersey-2-validation-errors-does-not-invoke-the-exceptionmapper
   */
  @Provider
  public static class ConstraintViolationErrorMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    HttpServerRequest request;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
      return convertToErrorResponse(request, exception);
    }
  }

  private static Response convertToErrorResponse(HttpServerRequest request, Exception exception) {
    String requestId = request.getHeader(REQUEST_ID);
    requestId = ((requestId == null) ? request.getParam(REQUEST_ID) : requestId);
    int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    String message = exception.getMessage();
    Integer code = null;
    if (exception instanceof WebApplicationException) {
      status = ((WebApplicationException) exception).getResponse().getStatus();
    } else if (exception instanceof IllegalArgumentException) {
      status = Response.Status.PRECONDITION_FAILED.getStatusCode();
    } else if (exception instanceof ConstraintViolationException) {
      status = Response.Status.BAD_REQUEST.getStatusCode();
      code = ServiceError.INPUT_PARAMETER_ERROR.getCode();
    } else if (exception instanceof ServiceOperationException) {
      status = Response.Status.BAD_REQUEST.getStatusCode();
      code = ((ServiceOperationException) exception).getErrorCode();
    }

    Map<String, Object> body = Maps.newLinkedHashMap();
    body.put("requestId", requestId);
    if (code != null) {
      body.put("code", code);
    }
    if (message != null) {
      if (request.path().startsWith("/v2")) {
        body.put("message", message);
      } else {
        body.put("error", message);
      }
    }

    return Response.status(status).entity(body).build();
  }

  private final static String STOPWATCH = "stopwatch";

  /**
   * It provides a access log recording for all REST requests. For more information:
   * <a href="https://quarkus.io/guides/rest-json"> https://quarkus.io/guides/rest-json</a>.
   */
  @Provider
  public static class LoggingRequestFilter implements ContainerRequestFilter {

    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext context) {
      context.setProperty(STOPWATCH, Stopwatch.createStarted());
      final String ri = request.getHeader("Request-Id");
      if (request.params().isEmpty()) {
        LOG.infof(
            "[Request] %s %s from ip: %s, req-id: %s",
            context.getMethod(),
            info.getPath(),
            request.remoteAddress(),
            ri
        );
      } else {
        LOG.infof(
            "[Request] %s %s from IP %s, params: {\n%s}, req-id: %s",
            context.getMethod(),
            info.getPath(),
            request.remoteAddress(),
            request.params(),
            ri
        );
      }
    }
  }

  /**
   * It provides a access log recording for all REST responses. For more information:
   * <a href="https://quarkus.io/guides/rest-json"> https://quarkus.io/guides/rest-json</a>.
   */
  @Provider
  public static class LoggingResponseFilter implements ContainerResponseFilter {

    @Context
    UriInfo info;

    @Context
    HttpServerResponse response;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(
        ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
      final String ri = request.getHeader("Request-Id");
      final Stopwatch sw = (Stopwatch) requestContext.getProperty(STOPWATCH);
      final String method = requestContext.getMethod();
      try {
        if (sw != null) { // will be null when 404 error happens
          sw.stop();
        }
        LOG.infof(
            "[Response] %s %s with result: %s, elapsed: %s, req-id: %s",
            method,
            info.getPath(),
            response.getStatusMessage(),
            sw,
            ri
        );
      } finally {
        requestContext.removeProperty(STOPWATCH);
      }
    }
  }
}
