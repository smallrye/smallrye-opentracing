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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.smallrye.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.smallrye.opentracing.contrib.jaxrs2.internal.SpanWrapper;

/**
 * Filter which finishes span after server processing. It is required to be registered.
 *
 * @author Pavol Loffay
 */
public class SpanFinishingFilter implements Filter {

    public SpanFinishingFilter() {
    }

    /**
     * @param tracer
     * @deprecated use no-args constructor
     */
    @Deprecated
    public SpanFinishingFilter(Tracer tracer) {
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            chain.doFilter(request, response);
        } catch (Exception ex) {
            SpanWrapper spanWrapper = getSpanWrapper(httpRequest);
            if (spanWrapper != null) {
                Tags.HTTP_STATUS.set(spanWrapper.get(), httpResponse.getStatus());
                addExceptionLogs(spanWrapper.get(), ex);
            }
            throw ex;
        } finally {
            SpanWrapper spanWrapper = getSpanWrapper(httpRequest);
            if (spanWrapper != null) {
                spanWrapper.getScope().close();
                if (request.isAsyncStarted()) {
                    request.getAsyncContext().addListener(new SpanFinisher(spanWrapper), request, response);
                } else {
                    spanWrapper.finish();
                }
            }
        }
    }

    private SpanWrapper getSpanWrapper(HttpServletRequest request) {
        return CastUtils.cast(request.getAttribute(SpanWrapper.PROPERTY_NAME), SpanWrapper.class);
    }

    @Override
    public void destroy() {
    }

    static class SpanFinisher implements AsyncListener {
        private SpanWrapper spanWrapper;

        SpanFinisher(SpanWrapper spanWrapper) {
            this.spanWrapper = spanWrapper;
        }

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            HttpServletResponse httpResponse = (HttpServletResponse) event.getSuppliedResponse();
            if (httpResponse.getStatus() >= 500) {
                addExceptionLogs(spanWrapper.get(), event.getThrowable());
            }
            Tags.HTTP_STATUS.set(spanWrapper.get(), httpResponse.getStatus());
            spanWrapper.finish();
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            // this handler is called when exception is thrown in async handler
            // note that exception logs are added in filter not here
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
        }
    }

    private static void addExceptionLogs(Span span, Throwable throwable) {
        Tags.ERROR.set(span, true);
        if (throwable != null) {
            Map<String, Object> errorLogs = new HashMap<>(2);
            errorLogs.put("event", Tags.ERROR.getKey());
            errorLogs.put("error.object", throwable);
            span.log(errorLogs);
        }
    }
}
