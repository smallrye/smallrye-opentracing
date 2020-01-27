package io.smallrye.opentracing;

import java.util.Map;

import org.eclipse.microprofile.context.spi.ThreadContextProvider;
import org.eclipse.microprofile.context.spi.ThreadContextSnapshot;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

public class SmallRyeOpenTracingContextProvider implements ThreadContextProvider {

    @Override
    public ThreadContextSnapshot currentContext(Map<String, String> props) {
        Tracer activeTracer = GlobalTracer.get();
        Span activeSpan = activeTracer.activeSpan();

        return () -> {
            Scope s = activeTracer.scopeManager().activate(activeSpan, false);
            return () -> {
                s.close();
            };
        };
    }

    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> props) {
        Tracer activeTracer = GlobalTracer.get();

        return () -> () -> {
            activeTracer.scopeManager().activate(null, false);
        };
    }

    @Override
    public String getThreadContextType() {
        return "OPENTRACING_CONTEXT";
    }
}