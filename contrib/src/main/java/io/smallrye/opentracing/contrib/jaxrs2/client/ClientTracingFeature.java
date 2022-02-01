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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.smallrye.opentracing.contrib.jaxrs2.serialization.InterceptorSpanDecorator;

/**
 * @author Pavol Loffay
 */
public class ClientTracingFeature implements Feature {
    private static final Logger log = Logger.getLogger(
            ClientTracingFeature.class.getName());

    private Builder builder;

    /**
     * When using this constructor application has to call {@link GlobalTracer#registerIfAbsent(Tracer)} to register
     * tracer instance.
     *
     * For a custom configuration use {@link Builder#build()}.
     */
    public ClientTracingFeature() {
        this(new Builder(GlobalTracer.get()));
    }

    private ClientTracingFeature(Builder builder) {
        this.builder = builder;
    }

    @Override
    public boolean configure(FeatureContext context) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Registering client OpenTracing, with configuration:" + builder.toString());
        }
        context.register(new ClientTracingFilter(builder.tracer, builder.spanDecorators),
                builder.priority);

        if (builder.traceSerialization) {
            context.register(
                    new ClientTracingInterceptor(builder.tracer, builder.serializationSpanDecorators),
                    builder.serializationPriority);
        }
        return true;
    }

    /**
     * Builder for configuring {@link Client} to trace outgoing requests.
     *
     * By default get's operation name is HTTP method and get is decorated with
     * {@link ClientSpanDecorator#STANDARD_TAGS} which adds set of standard tags.
     */
    public static class Builder {
        private Tracer tracer;
        private List<ClientSpanDecorator> spanDecorators;
        private List<InterceptorSpanDecorator> serializationSpanDecorators;
        private int priority;
        private int serializationPriority;
        private boolean traceSerialization;

        public Builder(Tracer tracer) {
            this.tracer = tracer;
            this.spanDecorators = Collections.singletonList(ClientSpanDecorator.STANDARD_TAGS);
            this.serializationSpanDecorators = Arrays.asList(InterceptorSpanDecorator.STANDARD_TAGS);
            // by default do not use Priorities.AUTHENTICATION due to security concerns
            this.priority = Priorities.HEADER_DECORATOR;
            this.serializationPriority = Priorities.ENTITY_CODER;
            this.traceSerialization = true;
        }

        /**
         * Set span decorators.
         * 
         * @return builder
         */
        public Builder withDecorators(List<ClientSpanDecorator> spanDecorators) {
            this.spanDecorators = spanDecorators;
            return this;
        }

        /**
         * Set serialization span decorators.
         * 
         * @return builder
         */
        public Builder withSerializationDecorators(List<InterceptorSpanDecorator> spanDecorators) {
            this.serializationSpanDecorators = spanDecorators;
            return this;
        }

        /**
         * @param priority the overriding priority for the registered component.
         *        Default is {@link Priorities#HEADER_DECORATOR}
         * @return builder
         *
         * @see Priorities
         */
        public Builder withPriority(int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * @param serializationPriority the overriding priority for the registered component.
         *        Default is {@link Priorities#ENTITY_CODER}
         * @return builder
         *
         * @see Priorities
         */
        public Builder withSerializationPriority(int serializationPriority) {
            this.serializationPriority = serializationPriority;
            return this;
        }

        /**
         * @param traceSerialization whether to trace serialization
         * @return builder
         */
        public Builder withTraceSerialization(boolean traceSerialization) {
            this.traceSerialization = traceSerialization;
            return this;
        }

        /**
         * @return client tracing feature. This feature should be manually registered to {@link Client}
         */
        public ClientTracingFeature build() {
            return new ClientTracingFeature(this);
        }
    }
}
