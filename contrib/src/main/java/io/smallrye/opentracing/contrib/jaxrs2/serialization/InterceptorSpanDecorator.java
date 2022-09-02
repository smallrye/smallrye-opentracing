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

import jakarta.ws.rs.ext.InterceptorContext;

import io.opentracing.Span;

public interface InterceptorSpanDecorator {

    /**
     * Decorate spans by outgoing object.
     *
     * @param context
     * @param span
     */
    void decorateRead(InterceptorContext context, Span span);

    /**
     * Decorate spans by outgoing object.
     *
     * @param context
     * @param span
     */
    void decorateWrite(InterceptorContext context, Span span);

    /**
     * Adds tags: \"media.type\", \"entity.type\"
     */
    InterceptorSpanDecorator STANDARD_TAGS = new InterceptorSpanDecorator() {
        @Override
        public void decorateRead(InterceptorContext context, Span span) {
            span.setTag("media.type", context.getMediaType().toString());
            span.setTag("entity.type", context.getType().getName());
        }

        @Override
        public void decorateWrite(InterceptorContext context, Span span) {
            decorateRead(context, span);
        }
    };
}
