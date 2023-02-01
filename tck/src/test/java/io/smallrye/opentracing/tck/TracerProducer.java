package io.smallrye.opentracing.tck;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;

/**
 * @author Pavol Loffay
 */
@ApplicationScoped
public class TracerProducer {

    @Default
    @Produces
    @Singleton
    public Tracer tracer() {
        return new MockTracer();
    }
}
