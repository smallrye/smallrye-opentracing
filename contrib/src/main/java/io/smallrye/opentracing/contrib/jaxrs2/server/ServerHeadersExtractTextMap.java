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

import java.util.Iterator;
import java.util.Map;

import jakarta.ws.rs.core.MultivaluedMap;

import io.opentracing.propagation.TextMap;
import io.smallrye.opentracing.contrib.web.servlet.filter.HttpServletRequestExtractAdapter;

/**
 * Helper class used to iterate over HTTP headers.
 *
 * @author Pavol Loffay
 */
public class ServerHeadersExtractTextMap implements TextMap {

    private final MultivaluedMap<String, String> headers;

    public ServerHeadersExtractTextMap(MultivaluedMap<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return new HttpServletRequestExtractAdapter.MultivaluedMapFlatIterator<>(headers.entrySet());
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException(
                ServerHeadersExtractTextMap.class.getName() + " should only be used with Tracer.extract()");
    }
}
