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

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Type;

import org.cloudy.fastjson.JSON;
import org.cloudy.fastjson.JSONException;
import org.cloudy.fastjson.parser.DefaultJSONParser;
import org.cloudy.fastjson.parser.JSONLexer;
import org.cloudy.fastjson.parser.JSONToken;
import org.cloudy.fastjson.parser.deserializer.ObjectDeserializer;

public class ColorCodec implements ObjectSerializer, ObjectDeserializer {

    public final static ColorCodec instance = new ColorCodec();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.getWriter();
        Color color = (Color) object;
        if (color == null) {
            out.writeNull();
            return;
        }

        char sep = '{';
        if (out.isEnabled(SerializerFeature.WriteClassName)) {
            out.write('{');
            out.writeFieldName(JSON.DEFAULT_TYPE_KEY);
            out.writeString(Color.class.getName());
            sep = ',';
        }

        out.writeFieldValue(sep, "r", color.getRed());
        out.writeFieldValue(',', "g", color.getGreen());
        out.writeFieldValue(',', "b", color.getBlue());
        if (color.getAlpha() > 0) {
            out.writeFieldValue(',', "alpha", color.getAlpha());
        }

        out.write('}');
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONLexer lexer = parser.getLexer();

        if (lexer.token() != JSONToken.LBRACE && lexer.token() != JSONToken.COMMA) {
            throw new JSONException("syntax error");
        }
        lexer.nextToken();

        int r = 0, g = 0, b = 0, alpha = 0;
        for (;;) {
            if (lexer.token() == JSONToken.RBRACE) {
                lexer.nextToken();
                break;
            }

            String key;
            if (lexer.token() == JSONToken.LITERAL_STRING) {
                key = lexer.stringVal();
                lexer.nextTokenWithColon(JSONToken.LITERAL_INT);
            } else {
                throw new JSONException("syntax error");
            }

            int val;
            if (lexer.token() == JSONToken.LITERAL_INT) {
                val = lexer.intValue();
                lexer.nextToken();
            } else {
                throw new JSONException("syntax error");
            }

            if (key.equalsIgnoreCase("r")) {
                r = val;
            } else if (key.equalsIgnoreCase("g")) {
                g = val;
            } else if (key.equalsIgnoreCase("b")) {
                b = val;
            } else if (key.equalsIgnoreCase("alpha")) {
                alpha = val;
            } else {
                throw new JSONException("syntax error, " + key);
            }

            if (lexer.token() == JSONToken.COMMA) {
                lexer.nextToken(JSONToken.LITERAL_STRING);
            }
        }

        return (T) new Color(r, g, b, alpha);
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }
}
