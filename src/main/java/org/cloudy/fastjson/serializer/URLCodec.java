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
import java.net.MalformedURLException;
import java.net.URL;

import org.cloudy.fastjson.JSONException;
import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.JSONToken;
import org.cloudy.fastjson.parser.deserializer.ObjectDeserializer;

public class URLCodec implements ObjectSerializer, ObjectDeserializer {

    public final static URLCodec instance = new URLCodec();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            serializer.writeNull();
            return;
        }

        serializer.write(object.toString());
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        
        String url = (String) parser.parse();
        
        if (url == null) {
            return null;
        }
        
        try {
            return (T) new URL(url);
        } catch (MalformedURLException e) {
            throw new JSONException("create url error", e);
        }
    }

    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}
