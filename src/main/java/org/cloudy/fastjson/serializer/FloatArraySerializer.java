/*
 * Copyright 2015-2115 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @email   dragonmail2001@163.com
 * @author  jinglong.zhaijl
 * @date    2015-10-24
 *
 */
package org.cloudy.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class FloatArraySerializer implements ObjectSerializer {

    public static final FloatArraySerializer instance = new FloatArraySerializer();

    public FloatArraySerializer(){
    }

    public final void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.getWriter();
        
        if (object == null) {
            if (out.isEnabled(SerializerFeature.WriteNullListAsEmpty)) {
                out.write("[]");
            } else {
                out.writeNull();
            }
            return;
        }

        float[] array = (float[]) object;
        int size = array.length;

        int end = size - 1;

        if (end == -1) {
            out.append("[]");
            return;
        }

        out.append('[');
        for (int i = 0; i < end; ++i) {
            float item = array[i];

            if (Float.isNaN(item)) {
                out.writeNull();
            } else {
                out.append(Float.toString(item));
            }

            out.append(',');
        }

        float item = array[end];

        if (Float.isNaN(item)) {
            out.writeNull();
        } else {
            out.append(Float.toString(item));
        }

        out.append(']');
    }
}
