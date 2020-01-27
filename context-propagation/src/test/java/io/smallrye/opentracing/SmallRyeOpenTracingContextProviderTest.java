package io.smallrye.opentracing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;

class SmallRyeOpenTracingContextProviderTest {

    private static final String OPENTRACING_CONTEXT = "OPENTRACING_CONTEXT";

    @BeforeAll
    static void setUpBeforeAll() {
        GlobalTracer.register(new MockTracer());
    }

    @BeforeEach
    void setUpBeforeEach() {
        Span activeSpan = GlobalTracer.get().activeSpan();
        if (activeSpan != null) {
            activeSpan.finish();
        }
    }

    @Test
    void ifNoSpanIsActiveAndOpenTracingContextIsPropagatedNoSpanShouldBePropagated() throws Exception {
        Span span = ManagedExecutor.builder()
                .propagated(OPENTRACING_CONTEXT)
                .build()
                .submit(() -> GlobalTracer.get().activeSpan())
                .get();

        assertEquals(null, span);
    }

    @Test
    void ifNoSpanIsActiveAndOpenTracingContextIsClearedNoSpanShouldBePropagated() throws Exception {
        Span span = ManagedExecutor.builder()
                .cleared(OPENTRACING_CONTEXT)
                .build()
                .submit(() -> GlobalTracer.get().activeSpan())
                .get();

        assertEquals(null, span);
    }

    @Test
    void ifSpanIsActiveAndOpenTracingContextIsPropagatedTheActiveSpanShouldBePropagated() throws Exception {
        try (Scope scope = GlobalTracer.get().buildSpan("test").startActive(true)) {
            Span activeSpan = scope.span();

            Span span = ManagedExecutor.builder()
                    .propagated(OPENTRACING_CONTEXT)
                    .build()
                    .submit(() -> GlobalTracer.get().activeSpan())
                    .get();

            assertEquals(activeSpan, span);
        }
    }

    @Test
    void ifSpanIsActiveAndOpenTracingContextIsClearedNoSpanShouldBePropagated() throws Exception {
        try (Scope scope = GlobalTracer.get().buildSpan("test").startActive(true)) {
            Span span = ManagedExecutor.builder()
                    .cleared(OPENTRACING_CONTEXT)
                    .build()
                    .submit(() -> GlobalTracer.get().activeSpan())
                    .get();

            assertEquals(null, span);
        }
    }
}
