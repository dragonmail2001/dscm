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
package org.cloudy.fastjson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.cloudy.fastjson.parser.Feature;
import org.cloudy.fastjson.serializer.SerializerFeature;

/**
 * @author wenshao[szujobs@hotmail.com]
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface JSONField {
    /**
     * config encode/decode ordinal
     * @since 1.1.42
     * @return
     */
    int ordinal() default 0;

    String name() default "";

    String format() default "";

    boolean serialize() default true;

    boolean deserialize() default true;

    SerializerFeature[] serialzeFeatures() default {};

    Feature[] parseFeatures() default {};
    
    String label() default "";
}
