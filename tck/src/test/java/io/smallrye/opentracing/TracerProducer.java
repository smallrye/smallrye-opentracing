package io.smallrye.opentracing;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;

/**
 * @author Pavol Loffay
 */
@ApplicationScoped
public class TracerProducer {

    @Default
    @Produces
    @Singleton
    public Tracer tracer() {
        MockTracer tracer = new MockTracer();
        GlobalTracer.register(tracer);
        return tracer;
    }
}
