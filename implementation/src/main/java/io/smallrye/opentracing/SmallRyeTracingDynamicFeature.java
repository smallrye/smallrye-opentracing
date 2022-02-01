package io.smallrye.opentracing;

import java.util.Optional;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.opentracing.Tracer;
import io.smallrye.opentracing.contrib.jaxrs2.server.OperationNameProvider.ClassNameOperationName;
import io.smallrye.opentracing.contrib.jaxrs2.server.OperationNameProvider.WildcardOperationName;
import io.smallrye.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.smallrye.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature.Builder;

/**
 * @author Pavol Loffay
 */
@Provider
public class SmallRyeTracingDynamicFeature implements DynamicFeature {

    private final ServerTracingDynamicFeature delegate;

    public SmallRyeTracingDynamicFeature() {
        Instance<Tracer> tracerInstance = CDI.current().select(Tracer.class);
        Config config = ConfigProvider.getConfig();
        Optional<String> skipPattern = config.getOptionalValue("mp.opentracing.server.skip-pattern", String.class);
        Optional<String> operationNameProvider = config.getOptionalValue("mp.opentracing.server.operation-name-provider",
                String.class);

        Builder builder = new Builder(tracerInstance.get())
                .withOperationNameProvider(ClassNameOperationName.newBuilder())
                .withTraceSerialization(false);
        if (skipPattern.isPresent()) {
            builder.withSkipPattern(skipPattern.get());
        }
        if (operationNameProvider.isPresent()) {
            if ("http-path".equalsIgnoreCase(operationNameProvider.get())) {
                builder.withOperationNameProvider(WildcardOperationName.newBuilder());
            } else if (!"class-method".equalsIgnoreCase(operationNameProvider.get())) {
                SmallRyeLogging.log.operationNameNotMatch();
            }
        }
        this.delegate = builder.build();
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        this.delegate.configure(resourceInfo, context);
    }
}
