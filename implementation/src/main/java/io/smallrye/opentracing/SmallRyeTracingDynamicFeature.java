package io.smallrye.opentracing;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.server.OperationNameProvider.ClassNameOperationName;
import io.opentracing.contrib.jaxrs2.server.OperationNameProvider.WildcardOperationName;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature.Builder;
import java.util.Optional;
import java.util.logging.Logger;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * @author Pavol Loffay
 */
@Provider
public class SmallRyeTracingDynamicFeature implements DynamicFeature {

  private static final Logger logger = Logger.getLogger(SmallRyeTracingDynamicFeature.class.getName());

  private final ServerTracingDynamicFeature delegate;

  public SmallRyeTracingDynamicFeature() {
    Instance<Tracer> tracerInstance = CDI.current().select(Tracer.class);
    Config config = ConfigProvider.getConfig();
    Optional<String> skipPattern = config.getOptionalValue("mp.opentracing.server.skip-pattern", String.class);
    Optional<String> operationNameProvider = config.getOptionalValue("mp.opentracing.server.operation-name-provider", String.class);

    Builder builder = new Builder(tracerInstance.get())
        .withOperationNameProvider(ClassNameOperationName.newBuilder())
        .withTraceSerialization(false);
    if (skipPattern.isPresent()) {
      builder.withSkipPattern(skipPattern.get());
    }
    if (operationNameProvider.isPresent()) {
       if ("http-path".equals(operationNameProvider.get())) {
         builder.withOperationNameProvider(WildcardOperationName.newBuilder());
       } else if (!"class-method".equals(operationNameProvider.get())) {
          logger.warning("Using unknown span operation name provider");
      }
    }
    this.delegate = builder.build();
  }

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
     this.delegate.configure(resourceInfo, context);
  }
}
