package io.smallrye.opentracing;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import io.opentracing.Tracer;
import io.smallrye.opentracing.contrib.jaxrs2.client.ClientTracingFeature;

/**
 * @author Pavol Loffay
 */
public class SmallRyeClientTracingFeature implements Feature {

    private final ClientTracingFeature delegate;

    public SmallRyeClientTracingFeature(Tracer tracer) {
        this.delegate = new ClientTracingFeature.Builder(tracer)
                .withTraceSerialization(false)
                .build();
    }

    @Override
    public boolean configure(FeatureContext context) {
        return this.delegate.configure(context);
    }
}
