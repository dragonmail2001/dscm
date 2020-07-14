/*
 * Copyright 2015 The Netty Project
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
package org.cloudy.netty.channel.epoll;

import org.cloudy.netty.channel.Channel;
import org.cloudy.netty.channel.unix.DomainSocketAddress;
import org.cloudy.netty.channel.unix.FileDescriptor;
import org.cloudy.netty.channel.unix.ServerDomainSocketChannel;
import org.cloudy.netty.channel.unix.Socket;
import org.cloudy.netty.util.internal.logging.InternalLogger;
import org.cloudy.netty.util.internal.logging.InternalLoggerFactory;

import java.io.File;
import java.net.SocketAddress;

import static org.cloudy.netty.channel.unix.Socket.newSocketDomain;


public final class EpollServerDomainSocketChannel extends AbstractEpollServerChannel
        implements ServerDomainSocketChannel {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(
            EpollServerDomainSocketChannel.class);

    private final EpollServerChannelConfig config = new EpollServerChannelConfig(this);
    private volatile DomainSocketAddress local;

    public EpollServerDomainSocketChannel() {
        super(newSocketDomain(), false);
    }

    /**
     * @deprecated Use {@link #EpollServerDomainSocketChannel(Socket, boolean)}.
     * Creates a new {@link EpollServerDomainSocketChannel} from an existing {@link FileDescriptor}.
     */
    public EpollServerDomainSocketChannel(FileDescriptor fd) {
        super(fd);
    }

    /**
     * @deprecated Use {@link #EpollServerDomainSocketChannel(Socket, boolean)}.
     */
    public EpollServerDomainSocketChannel(Socket fd) {
        super(fd);
    }

    public EpollServerDomainSocketChannel(Socket fd, boolean active) {
        super(fd, active);
    }

    @Override
    protected Channel newChildChannel(int fd, byte[] addr, int offset, int len) throws Exception {
        return new EpollDomainSocketChannel(this, new Socket(fd));
    }

    @Override
    protected DomainSocketAddress localAddress0() {
        return local;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        fd().bind(localAddress);
        fd().listen(config.getBacklog());
        local = (DomainSocketAddress) localAddress;
    }

    @Override
    protected void doClose() throws Exception {
        try {
            super.doClose();
        } finally {
            DomainSocketAddress local = this.local;
            if (local != null) {
                // Delete the socket file if possible.
                File socketFile = new File(local.path());
                boolean success = socketFile.delete();
                if (!success && logger.isDebugEnabled()) {
                    logger.debug("Failed to delete a domain socket file: {}", local.path());
                }
            }
        }
    }

    @Override
    public EpollServerChannelConfig config() {
        return config;
    }

    @Override
    public DomainSocketAddress remoteAddress() {
        return (DomainSocketAddress) super.remoteAddress();
    }

    @Override
    public DomainSocketAddress localAddress() {
        return (DomainSocketAddress) super.localAddress();
    }
}
