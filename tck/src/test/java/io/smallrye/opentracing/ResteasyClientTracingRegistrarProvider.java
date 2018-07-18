
package io.smallrye.opentracing;

import io.opentracing.Tracer;
import io.opentracing.contrib.concurrent.TracedExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.ClientBuilder;
import org.eclipse.microprofile.opentracing.ClientTracingRegistrarProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * @author Pavol Loffay
 */
public class ResteasyClientTracingRegistrarProvider implements ClientTracingRegistrarProvider {

  public ClientBuilder configure(ClientBuilder clientBuilder) {
    // Make sure executor is the same as a default in resteasy ClientBuilder
    return configure(clientBuilder, Executors.newFixedThreadPool(10));
  }

  public ClientBuilder configure(ClientBuilder clientBuilder, ExecutorService executorService) {
    ResteasyClientBuilder resteasyClientBuilder = (ResteasyClientBuilder)clientBuilder;
    Tracer tracer = CDI.current().select(Tracer.class).get();
    return resteasyClientBuilder.asyncExecutor(new TracedExecutorService(executorService, tracer))
      .register(new SmallRyeClientTracingFeature(tracer));
  }
}
