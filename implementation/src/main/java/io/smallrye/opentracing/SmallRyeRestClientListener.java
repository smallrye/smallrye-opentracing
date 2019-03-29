package io.smallrye.opentracing;

import io.opentracing.Tracer;
import javax.enterprise.inject.spi.CDI;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;

/**
 * @author Pavol Loffay
 */
public class SmallRyeRestClientListener implements RestClientListener {

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
      return;
    }
    restClientBuilder.register(new SmallRyeClientTracingFeature(tracer));
    restClientBuilder.register(new OpenTracingAsyncInterceptorFactory(tracer));
  }
}
