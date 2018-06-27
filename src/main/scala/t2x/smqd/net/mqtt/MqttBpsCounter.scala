// Copyright 2018 UANGEL
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package t2x.smqd.net.mqtt

import io.netty.buffer.{ByteBuf, ByteBufHolder}
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, ChannelPromise}
import io.netty.handler.traffic.{ChannelTrafficShapingHandler, TrafficCounter}

/**
  * 2018. 5. 30. - Created by Kwon, Yeong Eon
  */
object MqttBpsCounter {
  def apply(enableThrottling: Boolean, readLimit: Long, checkInterval: Long, maxTime: Long) =
    new MqttBpsCounter(enableThrottling, readLimit, checkInterval, maxTime)
}

@Sharable
class MqttBpsCounter(enableThrottling: Boolean, readLimit: Long, checkInterval: Long, maxTime: Long)
  extends ChannelTrafficShapingHandler(0, readLimit, checkInterval, maxTime) {

  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    val bytes = msg match {
      case msg: ByteBuf => msg.readableBytes
      case msg: ByteBufHolder => msg.content.readableBytes
      case _ => 1
    }

    val metrics = ctx.channel.attr(ATTR_METRICS).get
    metrics.byteReceived.inc(bytes)

    super.channelRead(ctx, msg)
  }

  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit = {
    val bytes = msg match {
      case msg: ByteBuf => msg.readableBytes()
      case msg: ByteBufHolder => msg.content().readableBytes()
      case _ => 1
    }

    val metrics = ctx.channel.attr(ATTR_METRICS).get
    metrics.byteSent.inc(bytes)

    super.write(ctx, msg, promise)
  }

  override def doAccounting(counter: TrafficCounter): Unit = {
    super.doAccounting(counter)
  }

}
