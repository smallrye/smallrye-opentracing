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

import java.util.List;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.InterceptorContext;

import io.opentracing.Tracer;
import io.smallrye.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.smallrye.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import io.smallrye.opentracing.contrib.jaxrs2.serialization.InterceptorSpanDecorator;
import io.smallrye.opentracing.contrib.jaxrs2.serialization.TracingInterceptor;

@Priority(Priorities.ENTITY_CODER)
public class ClientTracingInterceptor extends TracingInterceptor {

    public ClientTracingInterceptor(Tracer tracer, List<InterceptorSpanDecorator> spanDecorators) {
        super(tracer, spanDecorators);
    }

    @Override
    protected SpanWrapper findSpan(InterceptorContext context) {
        return CastUtils.cast(context.getProperty(PROPERTY_NAME), SpanWrapper.class);
    }
}
