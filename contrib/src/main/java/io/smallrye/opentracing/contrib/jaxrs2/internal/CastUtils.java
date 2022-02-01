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

import java.util.logging.Logger;

/**
 * @author Pavol Loffay
 */
public class CastUtils {
    private static final Logger log = Logger.getLogger(CastUtils.class.getName());

    private CastUtils() {
    }

    /**
     * Casts given object to the given class.
     *
     * @param object
     * @param clazz
     * @param <T>
     * @return casted object, or null if there is any error
     */
    public static <T> T cast(Object object, Class<T> clazz) {
        if (object == null || clazz == null) {
            return null;
        }

        try {
            return clazz.cast(object);
        } catch (ClassCastException ex) {
            log.severe("Cannot cast to " + clazz.getName());
            return null;
        }
    }
}
