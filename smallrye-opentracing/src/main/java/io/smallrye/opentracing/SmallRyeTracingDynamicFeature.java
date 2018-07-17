package io.smallrye.opentracing;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.server.OperationNameProvider.ClassNameOperationName;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature.Builder;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * @author Pavol Loffay
 */
@Provider
public class SmallRyeTracingDynamicFeature implements DynamicFeature {

  private ServerTracingDynamicFeature delegate;

  public SmallRyeTracingDynamicFeature() {
    Instance<Tracer> tracerInstance = CDI.current().select(Tracer.class);
    this.delegate = new Builder(tracerInstance.get())
        .withOperationNameProvider(ClassNameOperationName.newBuilder())
        .withTraceSerialization(false)
        .build();
  }

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
     delegate.configure(resourceInfo, context);
  }
}
