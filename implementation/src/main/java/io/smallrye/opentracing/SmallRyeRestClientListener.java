package io.smallrye.opentracing;

import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

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

        Tracer tracer = GlobalTracer.get();
        restClientBuilder.register(new SmallRyeClientTracingFeature(tracer));
        restClientBuilder.register(new OpenTracingAsyncInterceptorFactory(tracer));
    }
}
