package org.cloudy.dscm.client.handler;

import org.cloudy.netty.channel.Channel;

public interface CChannelInitializer {
	void initChannel(Channel ch) throws Exception;
}
