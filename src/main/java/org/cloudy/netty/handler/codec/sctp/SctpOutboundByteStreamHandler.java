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
package org.cloudy.netty.handler.codec.sctp;

import org.cloudy.netty.buffer.ByteBuf;
import org.cloudy.netty.channel.ChannelHandlerContext;
import org.cloudy.netty.channel.sctp.SctpMessage;
import org.cloudy.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * A ChannelHandler which transform {@link ByteBuf} to {@link SctpMessage}  and send it through a specific stream
 * with given protocol identifier.
 * Unordered delivery of all messages may be requested by passing unordered = true to the constructor.
 */
public class SctpOutboundByteStreamHandler extends MessageToMessageEncoder<ByteBuf> {
    private final int streamIdentifier;
    private final int protocolIdentifier;
    private final boolean unordered;

    /**
     * @param streamIdentifier      stream number, this should be >=0 or <= max stream number of the association.
     * @param protocolIdentifier    supported application protocol id.
     */
    public SctpOutboundByteStreamHandler(int streamIdentifier, int protocolIdentifier) {
        this(streamIdentifier, protocolIdentifier, false);
    }

    /**
     * @param streamIdentifier      stream number, this should be >=0 or <= max stream number of the association.
     * @param protocolIdentifier    supported application protocol id.
     * @param unordered             if {@literal true}, SCTP Data Chunks will be sent with the U (unordered) flag set.
     */
    public SctpOutboundByteStreamHandler(int streamIdentifier, int protocolIdentifier, boolean unordered) {
        this.streamIdentifier = streamIdentifier;
        this.protocolIdentifier = protocolIdentifier;
        this.unordered = unordered;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        out.add(new SctpMessage(streamIdentifier, protocolIdentifier, unordered, msg.retain()));
    }
}
