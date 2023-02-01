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

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;

import io.opentracing.Span;
import io.opentracing.tag.Tags;
import io.smallrye.opentracing.contrib.jaxrs2.internal.URIUtils;

/**
 * @author Pavol Loffay
 */
public interface ClientSpanDecorator {

    /**
     * Decorate get by incoming object.
     *
     * @param requestContext
     * @param span
     */
    void decorateRequest(ClientRequestContext requestContext, Span span);

    /**
     * Decorate spans by outgoing object.
     *
     * @param responseContext
     * @param span
     */
    void decorateResponse(ClientResponseContext responseContext, Span span);

    /**
     * Adds standard tags: {@link Tags#SPAN_KIND},
     * {@link Tags#PEER_HOSTNAME}, {@link Tags#PEER_PORT},
     * {@link Tags#HTTP_METHOD}, {@link Tags#HTTP_URL} and
     * {@link Tags#HTTP_STATUS}
     */
    ClientSpanDecorator STANDARD_TAGS = new ClientSpanDecorator() {
        @Override
        public void decorateRequest(ClientRequestContext requestContext, Span span) {
            Tags.COMPONENT.set(span, "jaxrs");
            Tags.PEER_HOSTNAME.set(span, requestContext.getUri().getHost());
            Tags.PEER_PORT.set(span, requestContext.getUri().getPort());

            Tags.HTTP_METHOD.set(span, requestContext.getMethod());

            String url = URIUtils.url(requestContext.getUri());
            if (url != null) {
                Tags.HTTP_URL.set(span, url);
            }
        }

        @Override
        public void decorateResponse(ClientResponseContext responseContext, Span span) {
            Tags.HTTP_STATUS.set(span, responseContext.getStatus());
        }
    };

    /**
     * As operation name provides HTTP path. If there are path parameters used in URL then
     * spans for the same requests would have different operation names, therefore use carefully.
     */
    ClientSpanDecorator HTTP_PATH_OPERATION_NAME = new ClientSpanDecorator() {
        @Override
        public void decorateRequest(ClientRequestContext clientRequestContext, Span span) {
            span.setOperationName(URIUtils.path(clientRequestContext.getUri()));
        }

        @Override
        public void decorateResponse(ClientResponseContext response, Span span) {
        }
    };
}
