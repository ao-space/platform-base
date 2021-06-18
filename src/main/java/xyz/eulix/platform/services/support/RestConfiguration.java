package xyz.eulix.platform.services.support;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
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

  /**
   * It provides exception result mapping for REST responses. For more information:
   * <a href="https://developer.jboss.org/docs/DOC-48310">https://developer.jboss.org/docs/DOC-48310</a>.
   */
  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
      int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
      String message = exception.getMessage();
      Integer code = null;
      if (exception instanceof WebApplicationException) {
        status = ((WebApplicationException) exception).getResponse().getStatus();
      } else if (exception instanceof IllegalArgumentException) {
        status = Response.Status.PRECONDITION_FAILED.getStatusCode();
      } else if (exception instanceof ConstraintViolation) {
        status = Response.Status.FORBIDDEN.getStatusCode();
      } else if (exception instanceof ServiceOperationException) {
        status = Response.Status.BAD_REQUEST.getStatusCode();
        code = ((ServiceOperationException) exception).getErrorCode();
      }

      Map<String, Object> body = Maps.newLinkedHashMap();
      body.put("exception", exception.getClass().getName());
      if (code != null) {
        body.put("code", code);
      }
      if (message != null) {
        body.put("error", message);
      }

      return Response.status(status).entity(body).build();
    }
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

    @Inject
    OperationUtils utils;

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
