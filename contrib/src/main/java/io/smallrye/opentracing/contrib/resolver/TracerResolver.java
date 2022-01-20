/*
 * Copyright 2017-2019 The OpenTracing Authors
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

import static io.smallrye.opentracing.contrib.resolver.PriorityComparator.prioritize;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.opentracing.Tracer;

/**
 * {@code TracerResolver} API definition looks for one or more registered {@link TracerResolver} implementations
 * using the {@link ServiceLoader}.
 * <p>
 * If no {@link TracerResolver} implementations are found, the {@link #resolveTracer()} method will fallback to
 * {@link ServiceLoader} lookup of the {@link Tracer} service itself.
 * <p>
 * Available {@link TracerConverter} implementations are applied to the resolved {@link Tracer} instance.
 * <p>
 * None of this happens if there is an existing {@code GlobalTracer} explicit registration.
 * That will always be returned (as-is) by the resolver, if available.
 *
 * @author Sjoerd Talsma
 */
public abstract class TracerResolver {
    private static final Logger LOGGER = Logger.getLogger(TracerResolver.class.getName());

    /**
     * Resolves the {@link Tracer} implementation.
     *
     * @return The resolved Tracer or {@code null} if none was resolved.
     * @deprecated Tracers are encouraged to use the {@link TracerFactory} interface to provide tracers
     */
    @Deprecated
    protected abstract Tracer resolve();

    /**
     * Attempts to resolve a Tracer via {@link ServiceLoader} using a variety of mechanisms, from the most recommended
     * to the least recommended:
     * <p>
     * <ul>
     * <li>based on the available {@link TracerFactory}</li>
     * <li>based on subclasses of {@link TracerResolver}</li>
     * <li>based on classes of {@link Tracer}</li>
     * </ul>
     *
     * <p>
     * Whenever a Tracer can be resolved by any of the methods above, the resolution stops. It means that if a Factory
     * is found, no Resolvers are attempted to be loaded.
     *
     * <p>
     * If a {@code GlobalTracer} has been previously registered, it will be returned before attempting to resolve
     * a {@linkplain Tracer} on our own.
     *
     * <p>
     * If more than one {@link TracerFactory} or {@link TracerResolver} is found, the one with the highest priority is
     * returned. Note that if a {@link TracerResolver} has a higher priority than all available {@link TracerFactory},
     * the factory still wins.
     *
     * @return The resolved Tracer or {@code null} if none was resolved.
     */
    public static Tracer resolveTracer() {
        return resolveTracer(null);
    }

    /**
     * Attempts to resolve a Tracer via {@link ServiceLoader} using a variety of mechanisms, from the most recommended
     * to the least recommended:
     * <p>
     * <ul>
     * <li>based on the available {@link TracerFactory}</li>
     * <li>based on subclasses of {@link TracerResolver}</li>
     * <li>based on classes of {@link Tracer}</li>
     * </ul>
     *
     * <p>
     * Whenever a Tracer can be resolved by any of the methods above, the resolution stops. It means that if a Factory
     * is found, no Resolvers are attempted to be loaded.
     *
     * <p>
     * If a {@code GlobalTracer} has been previously registered, it will be returned before attempting to resolve
     * a {@linkplain Tracer} on our own.
     *
     * <p>
     * If more than one {@link TracerFactory} or {@link TracerResolver} is found, the one with the highest priority is
     * returned. Note that if a {@link TracerResolver} has a higher priority than all available {@link TracerFactory},
     * the factory still wins.
     *
     * @param classloader The class loader to be used to load provider-configuration files
     *        and provider classes, or null if the thread context class loader to be used.
     * @return The resolved Tracer or {@code null} if none was resolved.
     */
    public static Tracer resolveTracer(ClassLoader classloader) {
        try { // Take care NOT to import GlobalTracer as it is an optional dependency and may not be on the classpath.
            if (io.opentracing.util.GlobalTracer.isRegistered()) {
                return logResolved(io.opentracing.util.GlobalTracer.get());
            }
        } catch (NoClassDefFoundError globalTracerNotInClasspath) {
            LOGGER.finest("GlobalTracer is not found on the classpath.");
        }

        Tracer tracer = null;
        if (!TracerResolver.isDisabled()) {
            if (classloader == null) {
                classloader = Thread.currentThread().getContextClassLoader();
            }
            tracer = getFromFactory(classloader);
            if (null == tracer) {
                tracer = getFromResolver(classloader);
            }

            if (null == tracer) {
                tracer = getFromServiceLoader(classloader);
            }
        }

        return tracer;
    }

    /**
     * Reloads the lazily found {@linkplain TracerResolver resolvers} and the fallback resolver.
     *
     * @deprecated This method is now no-op. It's safe to just remove this method call, as there's no caching anymore.
     */
    @Deprecated
    public static void reload() {
        LOGGER.log(Level.FINER, "No-op for this implementation.");
    }

    /**
     * There are two ways to globally disable the tracer resolver:
     * <ul>
     * <li>Setting a {@code "tracerresolver.disabled"} system property to {@code true}</li>
     * <li>Setting the environment variable {@code TRACERRESOLVER_DISABLED} to {@code true}</li>
     * </ul>
     *
     * @return Whether the tracer resolver mechanism is disabled ({@code false} by default).
     */
    private static boolean isDisabled() {
        String prop = System.getProperty("tracerresolver.disabled", System.getenv("TRACERRESOLVER_DISABLED"));
        return prop != null && (prop.equals("1") || prop.equalsIgnoreCase("true"));
    }

    private static Tracer convert(Tracer resolved) {
        if (resolved != null) {
            for (TracerConverter converter : prioritize(ServiceLoader.load(TracerConverter.class))) {
                try {
                    Tracer converted = converter.convert(resolved);
                    LOGGER.log(Level.FINEST, "Converted {0} using {1}: {2}.", new Object[] { resolved, converter, converted });
                    resolved = converted;
                } catch (RuntimeException rte) {
                    LOGGER.log(Level.WARNING, "Error converting " + resolved + " with " + converter + ": " + rte.getMessage(),
                            rte);
                }
                if (resolved == null)
                    break;
            }
        }
        return resolved;
    }

    private static Tracer logResolved(Tracer resolvedTracer) {
        LOGGER.log(Level.FINER, "Resolved tracer: {0}.", resolvedTracer);
        return resolvedTracer;
    }

    /**
     * Attempts to load a Tracer based on the {@link TracerFactory} interface. This is the preferred way to load a tracer
     * 
     * @param classloader The class loader to be used to load provider-configuration files
     *        and provider classes
     * @return a tracer as resolved by the classpath's TracerFactory, or null
     */
    private static Tracer getFromFactory(ClassLoader classloader) {
        for (TracerFactory factory : prioritize(ServiceLoader.load(TracerFactory.class, classloader))) {
            try {
                Tracer tracer = convert(factory.getTracer());
                if (tracer != null) {
                    return logResolved(tracer);
                }
            } catch (RuntimeException rte) {
                LOGGER.log(Level.WARNING, "Error getting tracer using " + factory + ": " + rte.getMessage(), rte);
            }
        }

        return null;
    }

    /**
     * Attempts to load a Tracer based on the TracerResolver class. This is the deprecated behavior and is kept here
     * for backwards compatibility reasons.
     *
     * @param classloader The class loader to be used to load provider-configuration files
     *        and provider classes
     * @return a tracer from {@link #resolve()}, or null
     */
    private static Tracer getFromResolver(ClassLoader classloader) {
        for (TracerResolver resolver : prioritize(ServiceLoader.load(TracerResolver.class, classloader))) {
            try {
                Tracer tracer = convert(resolver.resolve());
                if (tracer != null) {
                    return logResolved(tracer);
                }
            } catch (RuntimeException rte) {
                LOGGER.log(Level.WARNING, "Error resolving tracer using " + resolver + ": " + rte.getMessage(), rte);
            }
        }

        return null;
    }

    /**
     * Attempts to load a Tracer directly from the ServiceLoader.
     * 
     * @param classloader The class loader to be used to load provider-configuration files
     *        and provider classes
     * @return a tracer as resolved directly by the service loader, or null
     */
    private static Tracer getFromServiceLoader(ClassLoader classloader) {
        for (Tracer tracer : prioritize(ServiceLoader.load(Tracer.class, classloader))) {
            tracer = convert(tracer);
            if (tracer != null) {
                return logResolved(tracer);
            }
        }

        return null;
    }

}
