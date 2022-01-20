/*
 * Copyright 2018-2020 The OpenTracing Authors
 * Copied from https://github.com/opentracing-contrib/java-interceptors
 * Intended only for Jakarta namespace migration
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.smallrye.opentracing.contrib.interceptor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.Path;

import org.eclipse.microprofile.opentracing.Traced;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.smallrye.opentracing.contrib.resolver.TracerResolver;

@Traced
@Interceptor
@Priority(value = Interceptor.Priority.LIBRARY_BEFORE + 1)
public class OpenTracingInterceptor {
    public static final String SPAN_CONTEXT = "__opentracing_span_context";
    private static final Logger log = Logger.getLogger(OpenTracingInterceptor.class.getName());

    @Inject
    Instance<Tracer> tracerInstance;

    private volatile Tracer tracer = null;

    @AroundInvoke
    public Object wrap(InvocationContext ctx) throws Exception {
        if (skipJaxRs(ctx.getMethod())) {
            return ctx.proceed();
        }

        if (!traced(ctx.getMethod())) {
            return ctx.proceed();
        }

        Tracer tracer = getTracer();
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(getOperationName(ctx.getMethod()));

        int contextParameterIndex = -1;
        for (int i = 0; i < ctx.getParameters().length; i++) {
            Object parameter = ctx.getParameters()[i];
            if (parameter instanceof SpanContext) {
                log.fine("Found parameter as span context. Using it as the parent of this new span");
                spanBuilder.asChildOf((SpanContext) parameter);
                contextParameterIndex = i;
                break;
            }

            if (parameter instanceof Span) {
                log.fine("Found parameter as span. Using it as the parent of this new span");
                spanBuilder.asChildOf((Span) parameter);
                contextParameterIndex = i;
                break;
            }
        }

        if (contextParameterIndex < 0) {
            log.fine("No parent found. Trying to get span context from context data");
            Object ctxParentSpan = ctx.getContextData().get(SPAN_CONTEXT);
            if (ctxParentSpan instanceof SpanContext) {
                log.fine("Found span context from context data.");
                SpanContext parentSpan = (SpanContext) ctxParentSpan;
                spanBuilder.asChildOf(parentSpan);
            }
        }

        Span span = spanBuilder.start();
        Scope scope = tracer.activateSpan(span);
        try {
            log.fine("Adding span context into the invocation context.");
            ctx.getContextData().put(SPAN_CONTEXT, span.context());

            if (contextParameterIndex >= 0) {
                log.fine("Overriding the original span context with our new context.");
                for (int i = 0; i < ctx.getParameters().length; i++) {
                    if (ctx.getParameters()[contextParameterIndex] instanceof Span) {
                        ctx.getParameters()[contextParameterIndex] = span;
                    }

                    if (ctx.getParameters()[contextParameterIndex] instanceof SpanContext) {
                        ctx.getParameters()[contextParameterIndex] = span.context();
                    }
                }
            }

            return ctx.proceed();
        } catch (Exception e) {
            logException(span, e);
            throw e;
        } finally {
            span.finish();
            scope.close();
        }
    }

    // uses volatile read and synchronized block to avoid possible duplicate creation of Tracer in multi-threaded env
    public Tracer getTracer() {
        Tracer val = tracer;
        if (val != null) {
            return val;
        }
        synchronized (this) {
            if (tracer == null) {
                if (null != tracerInstance && !tracerInstance.isUnsatisfied()) {
                    tracer = this.tracerInstance.get();
                } else {
                    tracer = TracerResolver.resolveTracer();
                }
            }
            return tracer;
        }
    }

    private boolean traced(Method method) {
        Traced classTraced = method.getDeclaringClass().getAnnotation(Traced.class);
        Traced methodTraced = method.getAnnotation(Traced.class);
        if (methodTraced != null) {
            return methodTraced.value();
        }
        return classTraced != null && classTraced.value();
    }

    private boolean skipJaxRs(Method method) {
        return method.getAnnotation(Path.class) != null ||
                method.getDeclaringClass().getAnnotation(Path.class) != null;
    }

    private String getOperationName(Method method) {
        Traced classTraced = method.getDeclaringClass().getAnnotation(Traced.class);
        Traced methodTraced = method.getAnnotation(Traced.class);
        if (methodTraced != null && methodTraced.operationName().length() > 0) {
            return methodTraced.operationName();
        } else if (classTraced != null && classTraced.operationName().length() > 0) {
            return classTraced.operationName();
        }
        return String.format("%s.%s", method.getDeclaringClass().getName(), method.getName());
    }

    private void logException(Span span, Exception e) {
        Map<String, Object> errorLogs = new HashMap<String, Object>(2);
        errorLogs.put("event", Tags.ERROR.getKey());
        errorLogs.put("error.object", e);
        span.log(errorLogs);
        Tags.ERROR.set(span, true);
    }
}
