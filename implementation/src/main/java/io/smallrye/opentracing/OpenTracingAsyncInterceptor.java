package io.smallrye.opentracing;

import java.util.concurrent.CountDownLatch;

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

/**
 * @author Pavol Loffay
 */
public class OpenTracingAsyncInterceptor implements AsyncInvocationInterceptor {

    private final Tracer tracer;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private Span span;
    private Scope scope;

    public OpenTracingAsyncInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void prepareContext() {
        span = tracer.activeSpan();
        countDownLatch.countDown();
    }

    @Override
    public void applyContext() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        if (span != null) {
            scope = tracer.scopeManager().activate(span, false);
        }
    }

    @Override
    public void removeContext() {
        if (scope != null) {
            scope.close();
        }
    }
}
