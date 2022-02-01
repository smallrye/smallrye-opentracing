/*
 * Copyright 2016-2020 The OpenTracing Authors
 * Copied from https://github.com/opentracing-contrib/java-jaxrs
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
package io.smallrye.opentracing.contrib.jaxrs2.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.InterceptorContext;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopSpan;
import io.opentracing.tag.Tags;
import io.smallrye.opentracing.contrib.jaxrs2.internal.SpanWrapper;

public abstract class TracingInterceptor implements WriterInterceptor, ReaderInterceptor {
    private final Tracer tracer;
    private final Collection<InterceptorSpanDecorator> spanDecorators;

    public TracingInterceptor(Tracer tracer,
            List<InterceptorSpanDecorator> spanDecorators) {
        Objects.requireNonNull(tracer);
        Objects.requireNonNull(spanDecorators);
        this.tracer = tracer;
        this.spanDecorators = new ArrayList<>(spanDecorators);
    }

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context)
            throws IOException, WebApplicationException {
        Span span = buildSpan(context, "deserialize");
        try (Scope scope = tracer.activateSpan(span)) {
            decorateRead(context, span);
            try {
                return context.proceed();
            } catch (Exception e) {
                //TODO add exception logs in case they are not added by the filter.
                Tags.ERROR.set(span, true);
                throw e;
            }
        } finally {
            span.finish();
        }
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
            throws IOException, WebApplicationException {
        Span span = buildSpan(context, "serialize");
        try (Scope scope = tracer.activateSpan(span)) {
            decorateWrite(context, span);
            context.proceed();
        } catch (Exception e) {
            Tags.ERROR.set(span, true);
            throw e;
        } finally {
            span.finish();
        }
    }

    /**
     * Client requests :
     * <ul>
     * <li>Serialization of request body happens between the tracing filter invocation so we can use child_of.</li>
     * <li>Deserialization happens after the request is processed by the client filter therefore we can use follows_from
     * only.</li>
     * </ul>
     * Server requests :
     * <ul>
     * <li>Deserialization happens between the span in the server filter is started and finished so we can use child_of.</li>
     * <li>Serialization of response entity happens after the server span if finished so we can use only follows_from.</li>
     * </ul>
     * 
     * @param context Used to retrieve the current span wrapper created by the jax-rs request filter.
     * @param operationName "serialize" or "deserialize" depending on the context
     * @return a noop span is no span context is registered in the context. Otherwise a new span related to the current on
     *         retrieved from the context.
     */
    private Span buildSpan(InterceptorContext context, String operationName) {
        final SpanWrapper spanWrapper = findSpan(context);
        if (spanWrapper == null) {
            return NoopSpan.INSTANCE;
        }
        final Tracer.SpanBuilder spanBuilder = tracer.buildSpan(operationName);
        if (spanWrapper.isFinished()) {
            spanBuilder.addReference(References.FOLLOWS_FROM, spanWrapper.get().context());
        } else {
            spanBuilder.asChildOf(spanWrapper.get());
        }
        return spanBuilder.start();
    }

    protected abstract SpanWrapper findSpan(InterceptorContext context);

    private void decorateRead(InterceptorContext context, Span span) {
        for (InterceptorSpanDecorator decorator : spanDecorators) {
            decorator.decorateRead(context, span);
        }
    }

    private void decorateWrite(InterceptorContext context, Span span) {
        for (InterceptorSpanDecorator decorator : spanDecorators) {
            decorator.decorateWrite(context, span);
        }
    }
}
