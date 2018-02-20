/*
 * Copyright 2016-2018 Nextop Co.,Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nextop.lite.pool.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static cn.nextop.lite.pool.util.Strings.EMPTY;
import static cn.nextop.lite.pool.util.Strings.isEmpty;

/**
 * @author Baoyi Chen
 */
public class Classes {

    private static final Map<String, String> abbrs;

    static {
        final Map<String, String> m = new HashMap<>();
        m.put("I","int"); m.put("J","long"); m.put("B","byte");
        m.put("C","char"); m.put("V","void"); m.put("F","float");
        m.put("S","short"); m.put("D","double"); m.put("Z","boolean");
        abbrs = Collections.unmodifiableMap(m);
    }

    public static String getShortClassName(String clazz) {
        if (isEmpty(clazz)) return EMPTY;
        final StringBuilder prefix = new StringBuilder();
        if (clazz.startsWith("[")) {
            while (clazz.charAt(0) == '[') {
                clazz = clazz.substring(1); prefix.append("[]");
            }
            final int last = clazz.length() - 1;
            if (clazz.charAt(0) == 'L' && clazz.charAt(last) == ';') {
                clazz = clazz.substring(1, last);
            }
            if(abbrs.containsKey(clazz)) { clazz = abbrs.get(clazz); }
        }
        int i = clazz.lastIndexOf('.'); if((i == -1)) i = 0; else i++;
        int j = clazz.indexOf('$', i); String rs = clazz.substring(i);
        if(j != -1) { rs = rs.replace('$', '.'); } return rs + prefix;
    }

    public static String getShortClassName(Class<?> cls) {
        return cls == null ? EMPTY : getShortClassName(cls.getName());
    }
}
