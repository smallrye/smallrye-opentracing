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
package io.smallrye.opentracing.contrib.jaxrs2.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import io.opentracing.Scope;
import io.opentracing.Span;

/**
 * Wrapper class used for exchanging span between filters.
 *
 * @author Pavol Loffay
 */
public class SpanWrapper {

    public static final String PROPERTY_NAME = SpanWrapper.class.getName() + ".activeSpanWrapper";

    private Scope scope;
    private Span span;
    private AtomicBoolean finished = new AtomicBoolean();

    public SpanWrapper(Span span, Scope scope) {
        this.span = span;
        this.scope = scope;

    }

    public Span get() {
        return span;
    }

    public Scope getScope() {
        return scope;
    }

    public synchronized void finish() {
        if (!finished.get()) {
            finished.set(true);
            span.finish();
        }
    }

    public boolean isFinished() {
        return finished.get();
    }
}
