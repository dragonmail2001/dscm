package org.cloudy.dscm.client.handler;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudy.dscm.client.pool.CChannelPool;
import org.cloudy.netty.channel.ChannelHandlerContext;
import org.cloudy.netty.channel.SimpleChannelInboundHandler;
import org.cloudy.netty.handler.codec.http.HttpContent;
import org.cloudy.netty.handler.codec.http.HttpObject;
import org.cloudy.netty.handler.codec.http.HttpResponse;
import org.cloudy.netty.handler.codec.http.LastHttpContent;
import org.cloudy.netty.handler.timeout.IdleStateEvent;

public class CChannelPoolHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = Logger.getLogger(CChannelPoolHandler.class.getName());

    private CChannelPool    channelPool;

    /**
     * @param channelPool
     */
    public CChannelPoolHandler(CChannelPool channelPool) {
        super();
        this.channelPool = channelPool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
//        if (msg instanceof HttpResponse) {
//            HttpResponse headers = (HttpResponse) msg;
//            NettyHttpResponseFutureUtil.setPendingResponse(ctx.channel(), headers);
//        }
//        if (msg instanceof HttpContent) {
//            HttpContent httpContent = (HttpContent) msg;
//            NettyHttpResponseFutureUtil.setPendingContent(ctx.channel(), httpContent);
//            if (httpContent instanceof LastHttpContent) {
//                boolean connectionClose = NettyHttpResponseFutureUtil
//                    .headerContainConnectionClose(ctx.channel());
//
//                NettyHttpResponseFutureUtil.done(ctx.channel());
//                //the maxKeepAliveRequests config will cause server close the channel, and return 'Connection: close' in headers                
//                if (!connectionClose) {
//                    channelPool.returnChannel(ctx.channel());
//                }
//            }
//        }
    }

    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#userEventTriggered(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            logger.log(Level.WARNING, "remove idle channel: " + ctx.channel());
            ctx.channel().close();
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    /**
     * @param channelPool
     *            the channelPool to set
     */
    public void setChannelPool(CChannelPool channelPool) {
        this.channelPool = channelPool;
    }
}

