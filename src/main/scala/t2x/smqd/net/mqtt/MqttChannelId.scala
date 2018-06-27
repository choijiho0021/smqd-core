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

import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicLong

/**
  * 2018. 5. 30. - Created by Kwon, Yeong Eon
  */

object MqttChannelId {

  private val idSeq = new AtomicLong

  def apply(remoteAddr : InetSocketAddress): MqttChannelId = {
    val id: Long = idSeq.incrementAndGet
    new MqttChannelId(id, Some(remoteAddr.toString))
  }

  def apply(): MqttChannelId = {
    val id: Long = idSeq.incrementAndGet
    new MqttChannelId(id, None)
  }
}

class MqttChannelId(id: Long, remoteAddress: Option[String]) {
  override val hashCode: Int = (id ^ (id >>> 32)).toInt

  val stringId: String = "mqtt"+id

  override val toString: String = s"<mqtt-$id>"
}
