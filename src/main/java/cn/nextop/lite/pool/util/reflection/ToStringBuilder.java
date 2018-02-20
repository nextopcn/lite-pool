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

import static cn.nextop.lite.pool.util.reflection.ToStringBuilderStyle.INSTANCE;
import static cn.nextop.lite.pool.util.reflection.ToStringBuilderStyle.NULL_TEXT;

/**
 * @author apache commons-lang3 team
 */
public class ToStringBuilder {
    //
    protected final Object object;
    protected final StringBuilder builder;
    protected final ToStringBuilderStyle style = INSTANCE;

    /**
     *
     */
    public ToStringBuilder(Object object) {
        this.object = object;
        this.builder = new StringBuilder(512);
        this.style.appendSt(this.builder, object);
    }

    /**
     *
     */
    public ToStringBuilder append(char value) {
        style.append(builder, null, value); return this;
    }

    public ToStringBuilder append(float value) {
        style.append(builder, null, value); return this;
    }

    public ToStringBuilder append(short value) {
        style.append(builder, null, value); return this;
    }

    public ToStringBuilder append(double value) {
        style.append(builder, null, value); return this;
    }

    public ToStringBuilder append(boolean value) {
        style.append(builder, null, value); return this;
    }

    public ToStringBuilder append(int value) {
        style.append(builder, null, value); return this;
    }

    public ToStringBuilder append(byte value) {
        style.append(builder, null, value); return this;
    }

    public ToStringBuilder append(long value) {
        style.append(builder, null, value); return this;
    }

    public ToStringBuilder append(Object obj) {
        style.append(builder, null, obj, true); return this;
    }

    /**
     *
     */
    public ToStringBuilder append(boolean[] array) {
        style.append(builder, null, array, true); return this;
    }

    public ToStringBuilder append(byte[] array) {
        style.append(builder, null, array, true); return this;
    }

    public ToStringBuilder append(char[] array) {
        style.append(builder, null, array, true); return this;
    }

    public ToStringBuilder append(double[] array) {
        style.append(builder, null, array, true); return this;
    }

    public ToStringBuilder append(float[] array) {
        style.append(builder, null, array, true); return this;
    }

    public ToStringBuilder append(int[] array) {
        style.append(builder, null, array, true); return this;
    }

    public ToStringBuilder append(long[] array) {
        style.append(builder, null, array, true); return this;
    }

    public ToStringBuilder append(Object[] array) {
        style.append(builder, null, array, true); return this;
    }

    public ToStringBuilder append(short[] array) {
        style.append(builder, null, array, true); return this;
    }

    /**
     *
     */
    public ToStringBuilder append(String field, int value) {
        style.append(builder, field, value); return this;
    }

    public ToStringBuilder append(String field, byte value) {
        style.append(builder, field, value); return this;
    }

    public ToStringBuilder append(String field, Object obj) {
        style.append(builder, field, obj, true); return this;
    }

    public ToStringBuilder append(String field, char value) {
        style.append(builder, field, value); return this;
    }

    public ToStringBuilder append(String field, long value) {
        style.append(builder, field, value); return this;
    }

    public ToStringBuilder append(String field, short value) {
        style.append(builder, field, value); return this;
    }

    public ToStringBuilder append(String field, float value) {
        style.append(builder, field, value); return this;
    }

    public ToStringBuilder append(String field, double value) {
        style.append(builder, field, value); return this;
    }

    public ToStringBuilder append(String field, boolean value) {
        style.append(builder, field, value); return this;
    }

    /**
     *
     */
    public ToStringBuilder append(String field, int[] array) {
        style.append(builder, field, array, true); return this;
    }

    public ToStringBuilder append(String field, byte[] array) {
        style.append(builder, field, array, true); return this;
    }

    public ToStringBuilder append(String field, char[] array) {
        style.append(builder, field, array, true); return this;
    }

    public ToStringBuilder append(String field, long[] array) {
        style.append(builder, field, array, true); return this;
    }

    public ToStringBuilder append(String field, float[] array) {
        style.append(builder, field, array, true); return this;
    }

    public ToStringBuilder append(String field, short[] array) {
        style.append(builder, field, array, true); return this;
    }

    public ToStringBuilder append(String field, double[] array) {
        style.append(builder, field, array, true); return this;
    }

    public ToStringBuilder append(String field, Object[] array) {
        style.append(builder, field, array, true); return this;
    }

    public ToStringBuilder append(String field, boolean[] array) {
        style.append(builder, field, array, true); return this;
    }

    /**
     *
     */
    public ToStringBuilder append(String field, byte[] array, boolean detail) {
        style.append(builder, field, array, Boolean.valueOf(detail)); return this;
    }

    public ToStringBuilder append(String field, char[] array, boolean detail) {
        style.append(builder, field, array, Boolean.valueOf(detail)); return this;
    }

    public ToStringBuilder append(String field, int[] array, boolean detail) {
        style.append(builder, field, array, Boolean.valueOf(detail)); return this;
    }

    public ToStringBuilder append(String field, long[] array, boolean detail) {
        style.append(builder, field, array, Boolean.valueOf(detail)); return this;
    }

    public ToStringBuilder append(String field, short[] array, boolean detail) {
        style.append(builder, field, array, Boolean.valueOf(detail)); return this;
    }

    public ToStringBuilder append(String field, float[] array, boolean detail) {
        style.append(builder, field, array, Boolean.valueOf(detail)); return this;
    }

    public ToStringBuilder append(String field, Object[] array, boolean detail) {
        style.append(builder, field, array, Boolean.valueOf(detail)); return this;
    }

    public ToStringBuilder append(String field, boolean[] array, boolean detail) {
        style.append(builder, field, array, Boolean.valueOf(detail)); return this;
    }

    public ToStringBuilder append(String field, double[] array, boolean detail) {
        style.append(builder, field, array, Boolean.valueOf(detail)); return this;
    }

    public ToStringBuilder append(String field, Object object, boolean detail) {
        style.append(builder, field, object, Boolean.valueOf(detail)); return this;
    }

    /**
     *
     */
    @Override
    public String toString() {
        if (object == null) builder.append(NULL_TEXT);
        else this.style.appendEd(builder, object); return this.builder.toString();
    }
}
