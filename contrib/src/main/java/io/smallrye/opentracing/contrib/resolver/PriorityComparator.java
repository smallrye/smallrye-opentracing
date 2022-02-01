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

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Comparator for classes that may or may not contain a <code>{@literal @}Priority</code> annotation
 * on their class or superclasses.
 * <p>
 * The priority is applied as follows:
 * <ol>
 * <li>First, non-negative priority is applied in natural order (e.g. {@code 0}, {@code 1}, {@code 2}, ...).</li>
 * <li>Next, objects without <code>{@literal @}Priority</code> annotation are applied
 * by assigning a {@link #UNDEFINED_PRIORITY default priority} of {@link Integer#MAX_VALUE}.</li>
 * <li>Finally, negative priority is applied in reverse-natural order (e.g. {@code -1}, {@code -2}, {@code -3}, ...).</li>
 * </ol>
 * <p>
 * The order of objects with equal (implicit) priority is undefined.
 *
 * @author Sjoerd Talsma
 */
final class PriorityComparator implements Comparator<Object> {
    private static final int UNDEFINED_PRIORITY = Integer.MAX_VALUE;
    private static final boolean PRIORITY_AVAILABLE = isPriorityAnnotationAvailable();

    private static final PriorityComparator INSTANCE = new PriorityComparator();

    private PriorityComparator() {
    }

    static <T> Iterable<T> prioritize(Iterable<T> iterable) {
        if (!PRIORITY_AVAILABLE)
            return iterable;
        ArrayList<T> list = new ArrayList<T>();
        for (T value : iterable)
            list.add(value);
        Collections.sort(list, INSTANCE);
        return list;
    }

    @Override
    public int compare(Object value1, Object value2) {
        return comparePriority(priorityOf(value1), priorityOf(value2));
    }

    private static int comparePriority(int prio1, int prio2) {
        return prio1 == prio2 ? 0
                : prio1 < 0 ? (prio2 < 0 ? comparePriority(abs(prio1), abs(prio2)) : 1)
                        : prio2 < 0 ? -1
                                : prio1 < prio2 ? -1 : 1;
    }

    private static int priorityOf(Object value) {
        if (value == null)
            return UNDEFINED_PRIORITY;
        Class<?> type = value instanceof Class ? (Class<?>) value : value.getClass();
        // Don't import Priority. Loading the PriorityComparator class would fail if Priority isn't there at runtime!
        javax.annotation.Priority priority = type.getAnnotation(javax.annotation.Priority.class);
        return priority != null ? priority.value() : priorityOf(type.getSuperclass());
    }

    private static boolean isPriorityAnnotationAvailable() {
        try {
            return Class.forName("javax.annotation.Priority") != null;
        } catch (ClassNotFoundException cnfe) {
            return false;
        } catch (LinkageError le) {
            return false;
        }
    }

}
