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

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import static cn.nextop.lite.pool.util.Objects.getFieldValue;
import static cn.nextop.lite.pool.util.reflection.ToStringBuilderStyle.ARRAY_ED;
import static cn.nextop.lite.pool.util.reflection.ToStringBuilderStyle.ARRAY_SEPARATOR;
import static cn.nextop.lite.pool.util.reflection.ToStringBuilderStyle.ARRAY_ST;
import static cn.nextop.lite.pool.util.reflection.ToStringBuilderStyle.NULL_TEXT;
import static java.lang.reflect.AccessibleObject.setAccessible;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

/**
 * @author apache commons-lang3 team
 */
public class ToStringBuilderEx extends ToStringBuilder {

    public <T> ToStringBuilderEx(T obj) {
        super(obj);
    }

    public static String toString(Object object) {
        return new ToStringBuilderEx(object).toString();
    }

    protected boolean accept(Field field) {
        int modifiers = field.getModifiers(),idx = field.getName().indexOf('$');
        return ((idx == -1) && !isStatic(modifiers) && !isTransient(modifiers));
    }

    protected void appendField(Class<?> clazz) {
        if (clazz.isArray()) { appendArray(this.builder, this.object); return; }
        Field[] fields = clazz.getDeclaredFields(); setAccessible(fields, true);
        for (Field field : fields) {
            if (!this.accept(field)) continue;
            String n = field.getName(); Object v = getFieldValue(field, object);
            append(n, v);
        }
    }

    protected void appendArray(StringBuilder b, Object v) {
        b.append(ARRAY_ST);
        for (int len = Array.getLength(v), i = 0; i < len; i++) {
            Object item = Array.get(v, i); if (i > 0) b.append(ARRAY_SEPARATOR);
            if (item == null) this.style.appendNull(b, null);
            else this.style.appendInternal(b, null, item, true);
        }
        b.append(ARRAY_ED);
    }

    @Override
    public String toString() {
        if (this.object == null) return NULL_TEXT;
        Objects.getSuperClasses(object.getClass()).forEach(e -> appendField(e));
        return super.toString();
    }

}
