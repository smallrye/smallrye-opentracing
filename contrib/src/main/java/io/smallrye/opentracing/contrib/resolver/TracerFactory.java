/*
 * Copyright 2017-2018 The OpenTracing Authors
 * Copied from https://github.com/opentracing-contrib/java-tracerresolver
 * Intended only for Jakarta namespace migration
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.opentracing.contrib.resolver;

import java.util.ServiceLoader;

import io.opentracing.Tracer;

/**
 * Represents a class that knows how to select and build an appropriate tracer. The factory is usually used in
 * conjunction with the {@link TracerResolver}, but other resolver implementations can also load factories via
 * Java's {@link ServiceLoader}
 */
public interface TracerFactory {

    /**
     * Returns the concrete tracer implementation.
     *
     * @return the tracer instance
     */
    Tracer getTracer();
}
