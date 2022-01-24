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
package io.smallrye.opentracing.contrib.jaxrs2.server;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;

/**
 * @author Pavol Loffay
 */
public interface OperationNameProvider {
    interface Builder {
        OperationNameProvider build(Class<?> clazz, Method method);
    }

    String operationName(ContainerRequestContext requestContext);

    /**
     * Returns HTTP method as operation name
     */
    class HTTPMethodOperationName implements OperationNameProvider {
        static class Builder implements OperationNameProvider.Builder {
            @Override
            public OperationNameProvider build(Class<?> clazz, Method method) {
                return new HTTPMethodOperationName();
            }
        }

        HTTPMethodOperationName() {
        }

        @Override
        public String operationName(ContainerRequestContext requestContext) {
            return requestContext.getMethod();
        }

        public static Builder newBuilder() {
            return new Builder();
        }
    }

    /**
     * Default Microprofile operation name <HTTP method>:<package name>.<Class name>.<method name>
     */
    class ClassNameOperationName implements OperationNameProvider {
        static class Builder implements OperationNameProvider.Builder {
            @Override
            public OperationNameProvider build(Class<?> clazz, Method method) {
                return new ClassNameOperationName(clazz, method);
            }
        }

        private String classMethod;

        ClassNameOperationName(Class<?> clazz, Method method) {
            this.classMethod = String.format("%s.%s", clazz.getName(), method.getName());
        }

        @Override
        public String operationName(ContainerRequestContext requestContext) {
            return String.format("%s:%s", requestContext.getMethod(), classMethod);
        }

        public static Builder newBuilder() {
            return new Builder();
        }
    }

    /**
     * As operation name provides "wildcard" HTTP path e.g:
     *
     * resource method annotated with @Path("/foo/bar/{name: \\w+}") produces "/foo/bar/{name: \\w+}"
     *
     */
    class WildcardOperationName implements OperationNameProvider {
        static class Builder implements OperationNameProvider.Builder {
            @Override
            public OperationNameProvider build(Class<?> clazz, Method method) {
                String classPath = extractPath(clazz);
                String methodPath = extractPath(method);
                if (classPath == null || methodPath == null) {
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (classPath == null) {
                            String intfPath = extractPath(i);
                            if (intfPath != null) {
                                classPath = intfPath;
                            }
                        }
                        if (methodPath == null) {
                            for (Method m : i.getMethods()) {
                                if (m.getName() == method.getName()
                                        && Arrays.deepEquals(m.getParameterTypes(), method.getParameterTypes())) {
                                    methodPath = extractPath(m);
                                }
                            }
                        }
                    }
                }
                return new WildcardOperationName(classPath == null ? "" : classPath, methodPath == null ? "" : methodPath);
            }

            private static String extractPath(AnnotatedElement element) {
                Path path = element.getAnnotation(Path.class);
                if (path != null) {
                    return path.value();
                }
                return null;
            }
        }

        private final String path;

        WildcardOperationName(String clazz, String method) {
            if (clazz.isEmpty() || clazz.charAt(0) != '/') {
                clazz = "/" + clazz;
            }
            if (clazz.endsWith("/")) {
                clazz = clazz.substring(0, clazz.length() - 1);
            }
            if (method.isEmpty() || method.charAt(0) != '/') {
                method = "/" + method;
            }
            this.path = clazz + method;
        }

        @Override
        public String operationName(ContainerRequestContext requestContext) {
            return requestContext.getMethod() + ":" + path;
        }

        public static Builder newBuilder() {
            return new Builder();
        }
    }
}
