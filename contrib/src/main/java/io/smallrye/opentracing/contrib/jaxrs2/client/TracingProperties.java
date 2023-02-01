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

import io.opentracing.References;

/**
 * @author Pavol Loffay
 */
public class TracingProperties {

    private TracingProperties() {
    }

    /**
     * Denotes a parent span context {@link References#CHILD_OF}.
     * If it is not specified a new trace will be started.
     * Set on {@link jakarta.ws.rs.client.Invocation#property(String, Object)}.
     */
    public static final String CHILD_OF = ClientTracingFilter.class.getName() + "." + References.CHILD_OF;

    /**
     * Indicates whether request should be traced or not. If it is not
     * present and client is correctly configured request will be traced.
     * Value should be a boolean (trace disabled/enabled).
     * Set on {@link jakarta.ws.rs.client.Invocation#property(String, Object)}.
     */
    public static final String TRACING_DISABLED = ClientTracingFilter.class.getName() + ".tracingDisabled";
}
