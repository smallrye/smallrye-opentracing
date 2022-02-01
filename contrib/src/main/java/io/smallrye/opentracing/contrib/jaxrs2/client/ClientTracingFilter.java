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
package io.smallrye.opentracing.contrib.jaxrs2.client;

import static io.smallrye.opentracing.contrib.jaxrs2.internal.SpanWrapper.PROPERTY_NAME;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;

import org.eclipse.microprofile.opentracing.Traced;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopScopeManager.NoopScope;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.smallrye.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.smallrye.opentracing.contrib.jaxrs2.internal.SpanWrapper;

/**
 * @author Pavol Loffay
 */
@Priority(Priorities.HEADER_DECORATOR)
public class ClientTracingFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final Logger log = Logger.getLogger(
            ClientTracingFilter.class.getName());

    private Tracer tracer;
    private List<ClientSpanDecorator> spanDecorators;

    public ClientTracingFilter(Tracer tracer, List<ClientSpanDecorator> spanDecorators) {
        this.tracer = tracer;
        this.spanDecorators = new ArrayList<>(spanDecorators);
    }

    @Override
    public void filter(ClientRequestContext requestContext) {
        if (tracingDisabled(requestContext)) {
            log.finest("Client tracing disabled");
            return;
        }

        // in case filter is registered twice
        if (requestContext.getProperty(PROPERTY_NAME) != null) {
            return;
        }

        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(requestContext.getMethod())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

        SpanContext parentSpanContext = CastUtils.cast(requestContext.getProperty(TracingProperties.CHILD_OF),
                SpanContext.class);
        if (parentSpanContext != null) {
            spanBuilder.ignoreActiveSpan()
                    .asChildOf(parentSpanContext);
        }

        /**
         * Do not create Scope - there is no way to deactivate it on UnknownHostException
         */
        final Span span = spanBuilder.start();

        if (spanDecorators != null) {
            for (ClientSpanDecorator decorator : spanDecorators) {
                decorator.decorateRequest(requestContext, span);
            }
        }

        if (log.isLoggable(Level.FINEST)) {
            log.finest("Starting client span");
        }

        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new ClientHeadersInjectTextMap(requestContext.getHeaders()));
        requestContext.setProperty(PROPERTY_NAME, new SpanWrapper(span, new NoopScope() {
            @Override
            public void close() {
            }
        }));
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        SpanWrapper spanWrapper = CastUtils
                .cast(requestContext.getProperty(PROPERTY_NAME), SpanWrapper.class);
        if (spanWrapper != null && !spanWrapper.isFinished()) {
            log.finest("Finishing client span");

            if (spanDecorators != null) {
                for (ClientSpanDecorator decorator : spanDecorators) {
                    decorator.decorateResponse(responseContext, spanWrapper.get());
                }
            }

            spanWrapper.finish();
        }
    }

    private boolean tracingDisabled(ClientRequestContext clientRequestContext) {
        Boolean tracingDisabled = CastUtils.cast(clientRequestContext.getProperty(TracingProperties.TRACING_DISABLED),
                Boolean.class);
        if (tracingDisabled != null && tracingDisabled) {
            return true;
        }

        Object invokedMethod = clientRequestContext.getProperty("org.eclipse.microprofile.rest.client.invokedMethod");
        if (invokedMethod == null) {
            return false;
        }

        Method method = (Method) invokedMethod;
        Traced traced = method.getAnnotation(Traced.class);
        if (traced != null && !traced.value()) {
            return true;
        }
        return false;
    }
}
