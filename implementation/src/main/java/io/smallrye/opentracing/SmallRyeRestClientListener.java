package io.smallrye.opentracing;

import jakarta.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;

import io.opentracing.Tracer;

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

        Tracer tracer = CDI.current().select(Tracer.class).get();
        restClientBuilder.register(new SmallRyeClientTracingFeature(tracer));
        restClientBuilder.register(new OpenTracingAsyncInterceptorFactory(tracer));
    }
}
