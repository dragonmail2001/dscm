/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.cloudy.netty.handler.codec.http.multipart;

import org.cloudy.netty.buffer.ByteBuf;
import org.cloudy.netty.channel.ChannelException;
import org.cloudy.netty.handler.codec.http.HttpConstants;

import java.io.IOException;

import static org.cloudy.netty.buffer.Unpooled.*;

/**
 * Disk implementation of Attributes
 */
public class DiskAttribute extends AbstractDiskHttpData implements Attribute {
    public static String baseDirectory;

    public static boolean deleteOnExitTemporaryFile = true;

    public static final String prefix = "Attr_";

    public static final String postfix = ".att";

    /**
     * Constructor used for huge Attribute
     */
    public DiskAttribute(String name) {
        super(name, HttpConstants.DEFAULT_CHARSET, 0);
    }

    public DiskAttribute(String name, String value) throws IOException {
        super(name, HttpConstants.DEFAULT_CHARSET, 0); // Attribute have no default size
        setValue(value);
    }

    @Override
    public HttpDataType getHttpDataType() {
        return HttpDataType.Attribute;
    }

    @Override
    public String getValue() throws IOException {
        byte [] bytes = get();
        return new String(bytes, charset.name());
    }

    @Override
    public void setValue(String value) throws IOException {
        if (value == null) {
            throw new NullPointerException("value");
        }
        byte [] bytes = value.getBytes(charset.name());
        ByteBuf buffer = wrappedBuffer(bytes);
        if (definedSize > 0) {
            definedSize = buffer.readableBytes();
        }
        setContent(buffer);
    }

    @Override
    public void addContent(ByteBuf buffer, boolean last) throws IOException {
        int localsize = buffer.readableBytes();
        if (definedSize > 0 && definedSize < size + localsize) {
            definedSize = size + localsize;
        }
        super.addContent(buffer, last);
    }
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Attribute)) {
            return false;
        }
        Attribute attribute = (Attribute) o;
        return getName().equalsIgnoreCase(attribute.getName());
    }

    @Override
    public int compareTo(InterfaceHttpData o) {
        if (!(o instanceof Attribute)) {
            throw new ClassCastException("Cannot compare " + getHttpDataType() +
                    " with " + o.getHttpDataType());
        }
        return compareTo((Attribute) o);
    }

    public int compareTo(Attribute o) {
        return getName().compareToIgnoreCase(o.getName());
    }

    @Override
    public String toString() {
        try {
            return getName() + '=' + getValue();
        } catch (IOException e) {
            return getName() + "=IoException";
        }
    }

    @Override
    protected boolean deleteOnExit() {
        return deleteOnExitTemporaryFile;
    }

    @Override
    protected String getBaseDirectory() {
        return baseDirectory;
    }

    @Override
    protected String getDiskFilename() {
        return getName() + postfix;
    }

    @Override
    protected String getPostfix() {
        return postfix;
    }

    @Override
    protected String getPrefix() {
        return prefix;
    }

    @Override
    public Attribute copy() {
        DiskAttribute attr = new DiskAttribute(getName());
        attr.setCharset(getCharset());
        ByteBuf content = content();
        if (content != null) {
            try {
                attr.setContent(content.copy());
            } catch (IOException e) {
                throw new ChannelException(e);
            }
        }
        return attr;
    }

    @Override
    public Attribute duplicate() {
        DiskAttribute attr = new DiskAttribute(getName());
        attr.setCharset(getCharset());
        ByteBuf content = content();
        if (content != null) {
            try {
                attr.setContent(content.duplicate());
            } catch (IOException e) {
                throw new ChannelException(e);
            }
        }
        return attr;
    }

    @Override
    public Attribute retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public Attribute retain() {
        super.retain();
        return this;
    }
}
