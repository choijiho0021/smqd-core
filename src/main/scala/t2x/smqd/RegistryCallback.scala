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

package t2x.smqd

import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.StrictLogging
import t2x.smqd.ChiefActor.{Ready, ReadyAck}
import t2x.smqd.RegistryCallbackManagerActor.{CreateCallback, CreateCallbackPF}
import t2x.smqd.session.SessionActor.OutboundPublish

/**
  * 2018. 6. 18. - Created by Kwon, Yeong Eon
  */

object RegistryCallbackManagerActor {
  val actorName: String = "registry_callbacks"

  case class CreateCallback(callback: (TopicPath, Any) => Unit)
  case class CreateCallbackPF(partial: PartialFunction[(TopicPath, Any), Unit])
}

class RegistryCallbackManagerActor extends Actor with StrictLogging {
  override def receive: Receive = {
    case Ready =>
      context.become(receive0)
      sender ! ReadyAck
  }

  def receive0: Receive = {
    case CreateCallback(cb) =>
      val child = context.actorOf(Props(classOf[RegistryCallbackActor], cb))
      sender ! child
    case CreateCallbackPF(cb) =>
      val child = context.actorOf(Props(classOf[RegistryCallbackPFActor], cb))
      sender ! child
  }
}

/**
  * Actor works for callback function that subscribes a topic
  * @param callback callback function
  */
class RegistryCallbackActor(callback: (TopicPath, Any) => Unit) extends Actor with StrictLogging {
  override def receive: Receive = {
    case (topicPath: TopicPath, msg) =>
      try {
        callback(topicPath, msg)
      }
      catch{
        case e: Throwable =>
          logger.warn(s"Callback '${topicPath.toString}' throws an error", e)
      }
  }
}

/**
  * Actor works for partial function callback that subscribes a topic
  * @param callback callback partial function
  */
class RegistryCallbackPFActor(callback: PartialFunction[(TopicPath, Any), Unit]) extends Actor with StrictLogging {
  override def receive: Receive = {
    case (topicPath: TopicPath, msg) =>
      try {
        callback((topicPath, msg))
      }
      catch{
        case e: Throwable =>
          logger.warn(s"Callback '${topicPath.toString}' throws an error", e)
      }
  }
}
