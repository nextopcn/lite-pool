/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nextop.lite.pool.util.reflection;

import cn.nextop.lite.pool.util.Objects;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import static cn.nextop.lite.pool.util.Classes.getShortClassName;
import static cn.nextop.lite.pool.util.Objects.cast;
import static cn.nextop.lite.pool.util.Strings.delete;

/**
 * @author apache commons-lang3 team
 */
public class ToStringBuilderStyle {
    //
    public static final ToStringBuilderStyle INSTANCE = new ToStringBuilderStyle();
    protected static final ThreadLocal<WeakHashMap<Object, Object>> REGISTRY = new ThreadLocal<>();

    protected static final char ARRAY_ST = '{';
    protected static final char ARRAY_ED = '}';
    protected static final char CONTENT_ST = '[';
    protected static final char CONTENT_ED = ']';
    protected static final char SUMMARY_ST = '<';
    protected static final char SUMMARY_ED = '>';
    protected static final char SIZE_TEXT_ED = '>';
    protected static final char ATTR_SEPARATOR = '=';
    protected static final char FIELD_SEPARATOR = ',';
    protected static final char ARRAY_SEPARATOR = ',';

    protected static final String NULL_TEXT = "<null>";
    protected static final String SIZE_TEXT_ST = "<size=";

    /**
     *
     */
    public void appendSt(StringBuilder b, Object o) {
        if (o == null) return;
        appendClassName(b, o); appendContentSt(b);
    }

    public void appendEd(StringBuilder b, Object o) {
        final int i = b.length() - 1;
        delete(b, i, FIELD_SEPARATOR);
        appendContentEd(b); unregister(o);
    }

    protected void appendContentSt(StringBuilder b) {
        b.append(CONTENT_ST);
    }

    protected void appendContentEd(StringBuilder b) {
        b.append(CONTENT_ED);
    }

    protected void appendFieldSt(StringBuilder b, String f) {
        if (f != null) b.append(f).append(ATTR_SEPARATOR);
    }

    protected void appendFieldEd(StringBuilder b, String f) {
        b.append(FIELD_SEPARATOR);
    }

    protected void appendNull(StringBuilder builder, String field) {
        builder.append(NULL_TEXT);
    }

    protected void appendClassName(StringBuilder b, Object object) {
        if (object == null) return; register(object);
        b.append(getShortClassName(object.getClass()));
    }

    protected void appendCycleObject(StringBuilder b, String f, Object v) {
        Objects.identityToString(b, v);
    }

    protected void appendSummarySize(StringBuilder b, String f, int size) {
        b.append(SIZE_TEXT_ST).append(size).append(SIZE_TEXT_ED);
    }

    /**
     * 
     */
    static void register(Object v) {
        if (v == null) return; WeakHashMap<Object, Object> m = REGISTRY.get();
        if (m == null) REGISTRY.set((m = new WeakHashMap<>())); m.put(v, null);
    }

    static void unregister(Object v) {
        if (v == null) return; WeakHashMap<Object, Object> m = REGISTRY.get();
        if (m == null) return; m.remove(v); if (m.isEmpty()) REGISTRY.remove();
    }

    static boolean isRegistered(Object v) {
        Map<Object, Object> m = REGISTRY.get(); return m != null && m.containsKey(v);
    }

    /**
     * 
     */
    protected void appendInternal(StringBuilder b, String f, Object v, boolean detail) {
        boolean r = v instanceof Number || v instanceof Boolean || v instanceof Character;
        if (isRegistered(v) && !r) { appendCycleObject(b, f, v); return; }
        //
        register(v);
        try {
            if (v instanceof int[]) {
                if (detail) {
                    appendDetails(b, f, (int[]) v);
                } else {
                    appendSummary(b, f, (int[]) v);
                }

            } else if (v instanceof byte[]) {
                if (detail) {
                    appendDetails(b, f, (byte[]) v);
                } else {
                    appendSummary(b, f, (byte[]) v);
                }

            } else if (v instanceof char[]) {
                if (detail) {
                    appendDetails(b, f, (char[]) v);
                } else {
                    appendSummary(b, f, (char[]) v);
                }

            } else if (v instanceof long[]) {
                if (detail) {
                    appendDetails(b, f, (long[]) v);
                } else {
                    appendSummary(b, f, (long[]) v);
                }

            } else if (v instanceof short[]) {
                if (detail) {
                    appendDetails(b, f, (short[]) v);
                } else {
                    appendSummary(b, f, (short[]) v);
                }

            } else if (v instanceof float[]) {
                if (detail) {
                    appendDetails(b, f, (float[]) v);
                } else {
                    appendSummary(b, f, (float[]) v);
                }

            } else if (v instanceof double[]) {
                if (detail) {
                    appendDetails(b, f, (double[]) v);
                } else {
                    appendSummary(b, f, (double[]) v);
                }

            } else if (v.getClass().isArray()) {
                if (detail) {
                    appendDetails(b, f, (Object[]) v);
                } else {
                    appendSummary(b, f, (Object[]) v);
                }

            } else if (v instanceof Collection<?>) {
                Collection<?> c = cast(v);
                if (detail) {
                    appendDetails(b, f, c);
                } else {
                    appendSummarySize(b, f, c.size());
                }

            } else if (v instanceof Map<?, ?>) {
                Map<?, ?> m = cast(v);
                if (detail) {
                    appendDetails(b, f, m);
                } else {
                    appendSummarySize(b, f, m.size());
                }

            } else if (v instanceof boolean[]) {
                if (detail) {
                    appendDetails(b, f, (boolean[]) v);
                } else {
                    appendSummary(b, f, (boolean[]) v);
                }

            } else {
                if (detail) {
                    appendDetails(b, f, v);
                } else {
                    appendSummary(b, f, v);
                }
            }
        } finally {
            unregister(v);
        }
    }

    /**
     *
     */
    protected void appendDetails(StringBuilder b, String f, int v) {
        b.append(v);
    }

    protected void appendDetails(StringBuilder b, String f, long v) {
        b.append(v);
    }

    protected void appendDetails(StringBuilder b, String f, byte v) {
        b.append(v);
    }

    protected void appendDetails(StringBuilder b, String f, char v) {
        b.append(v);
    }

    protected void appendDetails(StringBuilder b, String f, short v) {
        b.append(v);
    }

    protected void appendDetails(StringBuilder b, String f, float v) {
        b.append(v);
    }

    protected void appendDetails(StringBuilder b, String f, double v) {
        b.append(v);
    }

    protected void appendDetails(StringBuilder b, String f, Object v) {
        b.append(v);
    }

    protected void appendDetails(StringBuilder b, String f, boolean v) {
        b.append(v);
    }

    protected void appendDetails(StringBuilder b, String f, Map<?, ?> m) {
        b.append(m);
    }

    protected void appendDetails(StringBuilder b, String f, Collection<?> c) {
        b.append(c);
    }

    /**
     *
     */
    protected void appendSummary(StringBuilder b, String f, int[] v) {
        appendSummarySize(b, f, v.length);
    }

    protected void appendSummary(StringBuilder b, String f, byte[] v) {
        appendSummarySize(b, f, v.length);
    }

    protected void appendSummary(StringBuilder b, String f, long[] v) {
        appendSummarySize(b, f, v.length);
    }

    protected void appendSummary(StringBuilder b, String f, char[] v) {
        appendSummarySize(b, f, v.length);
    }

    protected void appendSummary(StringBuilder b, String f, short[] v) {
        appendSummarySize(b, f, v.length);
    }

    protected void appendSummary(StringBuilder b, String f, float[] v) {
        appendSummarySize(b, f, v.length);
    }

    protected void appendSummary(StringBuilder b, String f, Object[] v) {
        appendSummarySize(b, f, v.length);
    }

    protected void appendSummary(StringBuilder b, String f, double[] v) {
        appendSummarySize(b, f, v.length);
    }

    protected void appendSummary(StringBuilder b, String f, boolean[] v) {
        appendSummarySize(b, f, v.length);
    }

    protected void appendSummary(StringBuilder b, String f, Object v) {
        final Class<?> c = v.getClass();
        b.append(SUMMARY_ST).append(getShortClassName(c)).append(SUMMARY_ED);
    }

    /**
     *
     */
    public void append(StringBuilder b, String f, Object v, boolean d) {
        appendFieldSt(b, f);
        if (v == null) appendNull(b, f); else appendInternal(b, f, v, d);
        appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, int v) {
        appendFieldSt(b, f); appendDetails(b, f, v); appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, byte v) {
        appendFieldSt(b, f); appendDetails(b, f, v); appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, long v) {
        appendFieldSt(b, f); appendDetails(b, f, v); appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, char v) {
        appendFieldSt(b, f); appendDetails(b, f, v); appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, float v) {
        appendFieldSt(b, f); appendDetails(b, f, v); appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, short v) {
        appendFieldSt(b, f); appendDetails(b, f, v); appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, double v) {
        appendFieldSt(b, f); appendDetails(b, f, v); appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, boolean v) {
        appendFieldSt(b, f); appendDetails(b, f, v); appendFieldEd(b, f);
    }

    /**
     *
     */
    protected void appendDetails(StringBuilder b, String f, int[] v) {
        b.append(ARRAY_ST);
        for (int i = 0; i < v.length; i++) {
            if (i > 0) b.append(ARRAY_SEPARATOR); appendDetails(b, f, v[i]);
        }
        b.append(ARRAY_ED);
    }

    protected void appendDetails(StringBuilder b, String f, long[] v) {
        b.append(ARRAY_ST);
        for (int i = 0; i < v.length; i++) {
            if (i > 0) b.append(ARRAY_SEPARATOR); appendDetails(b, f, v[i]);
        }
        b.append(ARRAY_ED);
    }

    protected void appendDetails(StringBuilder b, String f, byte[] v) {
        b.append(ARRAY_ST);
        for (int i = 0; i < v.length; i++) {
            if (i > 0) b.append(ARRAY_SEPARATOR); appendDetails(b, f, v[i]);
        }
        b.append(ARRAY_ED);
    }

    protected void appendDetails(StringBuilder b, String f, char[] v) {
        b.append(ARRAY_ST);
        for (int i = 0; i < v.length; i++) {
            if (i > 0) b.append(ARRAY_SEPARATOR); appendDetails(b, f, v[i]);
        }
        b.append(ARRAY_ED);
    }

    protected void appendDetails(StringBuilder b, String f, short[] v) {
        b.append(ARRAY_ST);
        for (int i = 0; i < v.length; i++) {
            if (i > 0) b.append(ARRAY_SEPARATOR); appendDetails(b, f, v[i]);
        }
        b.append(ARRAY_ED);
    }

    protected void appendDetails(StringBuilder b, String f, float[] v) {
        b.append(ARRAY_ST);
        for (int i = 0; i < v.length; i++) {
            if (i > 0) b.append(ARRAY_SEPARATOR); appendDetails(b, f, v[i]);
        }
        b.append(ARRAY_ED);
    }

    protected void appendDetails(StringBuilder b, String f, double[] v) {
        b.append(ARRAY_ST);
        for (int i = 0; i < v.length; i++) {
            if (i > 0) b.append(ARRAY_SEPARATOR); appendDetails(b, f, v[i]);
        }
        b.append(ARRAY_ED);
    }

    protected void appendDetails(StringBuilder b, String f, boolean[] v) {
        b.append(ARRAY_ST);
        for (int i = 0; i < v.length; i++) {
            if (i > 0) b.append(ARRAY_SEPARATOR); appendDetails(b, f, v[i]);
        }
        b.append(ARRAY_ED);
    }

    protected void appendDetails(StringBuilder b, String f, Object[] v) {
        b.append(ARRAY_ST);
        for (int i = 0; i < v.length; i++) {
            final Object item = v[i]; if (i > 0) b.append(ARRAY_SEPARATOR);
            if (item == null) appendNull(b, f); else appendInternal(b, f, item, true);
        }
        b.append(ARRAY_ED);
    }

    /**
     *
     */
    public void append(StringBuilder b, String f, Object[] v, boolean d) {
        appendFieldSt(b, f);
        if (v == null) appendNull(b, f); else if (d) appendDetails(b, f, v); else appendSummary(b, f, v);
        appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, long[] v, boolean d) {
        appendFieldSt(b, f);
        if (v == null) appendNull(b, f); else if (d) appendDetails(b, f, v); else appendSummary(b, f, v);
        appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, int[] v, boolean d) {
        appendFieldSt(b, f);
        if (v == null) appendNull(b, f); else if (d) appendDetails(b, f, v); else appendSummary(b, f, v);
        appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, short[] v, boolean d) {
        appendFieldSt(b, f);
        if (v == null) appendNull(b, f); else if (d) appendDetails(b, f, v); else appendSummary(b, f, v);
        appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, byte[] v, boolean d) {
        appendFieldSt(b, f);
        if (v == null) appendNull(b, f); else if (d) appendDetails(b, f, v); else appendSummary(b, f, v);
        appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, char[] v, boolean d) {
        appendFieldSt(b, f);
        if (v == null) appendNull(b, f); else if (d) appendDetails(b, f, v); else appendSummary(b, f, v);
        appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, double[] v, boolean d) {
        appendFieldSt(b, f);
        if (v == null) appendNull(b, f); else if (d) appendDetails(b, f, v); else appendSummary(b, f, v);
        appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, float[] v, boolean d) {
        appendFieldSt(b, f);
        if (v == null) appendNull(b, f); else if (d) appendDetails(b, f, v); else appendSummary(b, f, v);
        appendFieldEd(b, f);
    }

    public void append(StringBuilder b, String f, boolean[] v, boolean d) {
        appendFieldSt(b, f);
        if (v == null) appendNull(b, f); else if (d) appendDetails(b, f, v); else appendSummary(b, f, v);
        appendFieldEd(b, f);
    }

}

