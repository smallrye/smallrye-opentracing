package io.smallrye.opentracing;

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;

import io.opentracing.Tracer;

/**
 * @author Pavol Loffay
 */
public class OpenTracingAsyncInterceptorFactory implements AsyncInvocationInterceptorFactory {

    private Tracer tracer;

    public OpenTracingAsyncInterceptorFactory(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public AsyncInvocationInterceptor newInterceptor() {
        return new OpenTracingAsyncInterceptor(tracer);
    }
}
