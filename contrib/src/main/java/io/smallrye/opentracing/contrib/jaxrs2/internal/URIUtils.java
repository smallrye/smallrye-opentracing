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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * @author Pavol Loffay
 */
public class URIUtils {
    private URIUtils() {
    }

    /**
     * Returns path of given URI. If the first character of path is '/' then it is removed.
     *
     * @param uri
     * @return path or null
     */
    public static String path(URI uri) {
        String path = uri.getPath();
        if (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    /**
     * Returns string representation of supplied URL.
     *
     * @param uri
     * @return string URL or null
     */
    public static String url(URI uri) {
        String urlStr = null;
        try {
            URL url = uri.toURL();
            urlStr = url.toString();
        } catch (MalformedURLException e) {
            // ignoring returning null
        }

        return urlStr;
    }
}
