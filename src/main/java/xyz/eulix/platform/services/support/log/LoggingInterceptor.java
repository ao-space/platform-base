package xyz.eulix.platform.services.support.log;

import com.google.common.base.Stopwatch;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.support.serialization.OperationUtils;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * Provides the logging joint point to intercept the invocation of method or class
 * which is marked by the annotation {@code Logged}. For more information:
 * <a href="https://quarkus.io/guides/cdi#interceptors">https://quarkus.io/guides/cdi#interceptors</a>.
 *
 * @see Logged
 * @since 1.0.0
 * @author Haibo Luo
 */
@Logged
@Priority(200)
@Interceptor
@SuppressWarnings("unused") // Used by the framework
public class LoggingInterceptor {

  static final Logger LOG = Logger.getLogger("app.log");

  @Inject
  OperationUtils utils;

  @AroundInvoke
  Object logInvocation(InvocationContext context) {
    final String mn = context.getMethod().getName();
    LOG.infov(
        "[Invoke] method: {0}(), parameters: {1}",
        mn,
        utils.objectToJson(context.getParameters())
    );

    Stopwatch sw = Stopwatch.createStarted();
    Object ret;
    try {
      ret = doSneakyThrowsInvoke(context);
    } catch (Exception rethrow) {
      LOG.errorv(
          rethrow,
          "[Throw] method: {0}(), exception",
          mn
      );
      throw rethrow;
    } finally {
      sw.stop();
    }

    LOG.infov(
        "[Return] method: {0}(), result: {1}, elapsed: {2}",
        mn,
        utils.objectToJson(ret),
        sw
    );
    return ret;
  }

  @SneakyThrows
  Object doSneakyThrowsInvoke(InvocationContext context) {
    return context.proceed();
  }
}
