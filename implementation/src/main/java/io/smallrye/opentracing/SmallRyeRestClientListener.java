package io.smallrye.opentracing;

import io.opentracing.Tracer;
import javax.enterprise.inject.spi.CDI;
import java.util.logging.Logger;

import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;

/**
 * @author Pavol Loffay
 */
public class SmallRyeRestClientListener implements RestClientListener {

  private static final Logger logger = Logger.getLogger(SmallRyeRestClientListener.class.getName());

  @Override
  public void onNewClient(Class<?> clientInterface, RestClientBuilder restClientBuilder) {
    Traced traced = clientInterface.getAnnotation(Traced.class);
    if (traced != null && !traced.value()) {
      // tracing is disabled
      return;
    }

    Tracer tracer;
    try {
      tracer = CDI.current().select(Tracer.class).get();
    } catch (IllegalStateException e) {
      logger.warning("CDI container is unavailable");
      return;
    }
    restClientBuilder.register(new SmallRyeClientTracingFeature(tracer));
    restClientBuilder.register(new OpenTracingAsyncInterceptorFactory(tracer));
  }
}
