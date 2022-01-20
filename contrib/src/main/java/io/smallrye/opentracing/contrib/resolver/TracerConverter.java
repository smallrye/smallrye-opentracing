/*
 * Copyright 2017 The OpenTracing Authors
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

import io.opentracing.Tracer;

/**
 * Function converting an existing {@link Tracer}.
 * <p>
 * This can be useful for <em>wrapping</em> tracers:
 * 
 * <pre>
 * <code>
 * public final class FooWrapperConverter implements TracerConverter {
 *     public Tracer convert(Tracer existingTracer) {
 *         return new FooTracerWrapper(existingTracer);
 *     }
 * }
 * </code>
 * </pre>
 * <p>
 * If there are multiple {@linkplain TracerConverter} implementations resolved,
 * they will be applied in the order of their {@literal @}Priority annotation:
 * <ol>
 * <li>First, non-negative priority is applied in natural order (e.g. {@code 0}, {@code 1}, {@code 2}, ...).</li>
 * <li>Next, objects without <code>{@literal @}Priority</code> annotation are applied
 * by assigning a <em>default priority</em> of {@link Integer#MAX_VALUE}.</li>
 * <li>Finally, negative priority is applied in reverse-natural order (e.g. {@code -1}, {@code -2}, {@code -3}, ...).</li>
 * </ol>
 * <p>
 * The order of objects with equal (implicit) priority is undefined.
 *
 * @author Sjoerd Talsma
 */
public interface TracerConverter {

    /**
     * Function that converts a {@link Tracer}.
     * <p>
     * It may either manipulate the tracer or return an entirely new {@linkplain Tracer} instance.
     *
     * @param existingTracer The existing tracer to be converted.
     * @return The converted tracer.
     */
    Tracer convert(Tracer existingTracer);

}
