/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.apollo.broker.protocol

import java.io.IOException
import org.apache.activemq.apollo.broker.store.MessageRecord
import org.apache.activemq.apollo.transport._
import org.apache.activemq.apollo.dto.ConnectionStatusDTO
import org.apache.activemq.apollo.util.{Log, ClassFinder}
import org.apache.activemq.apollo.broker.{Broker, Message, BrokerConnection}

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object ProtocolFactory {

  trait Provider {
    def create():Protocol
    def create(config:String):Protocol
  }

  val provider = new ClassFinder[Provider]("META-INF/services/org.apache.activemq.apollo/protocol-factory.index",classOf[Provider])

  def get(name:String):Option[Protocol] = {
    provider.singletons.foreach { provider=>
      val rc = provider.create(name)
      if( rc!=null ) {
        return Some(rc)
      }
    }
    None
  }
}

trait Protocol extends ProtocolCodecFactory.Provider {

  def createProtocolHandler:ProtocolHandler
  def encode(message:Message):MessageRecord
  def decode(message:MessageRecord):Message

}

object ProtocolHandler extends Log

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
trait ProtocolHandler {
  import ProtocolHandler._

  def protocol:String

  var connection:BrokerConnection = null;

  def set_connection(brokerConnection:BrokerConnection) = {
    this.connection = brokerConnection
  }

  def create_connection_status = new ConnectionStatusDTO

  def on_transport_failure(error:IOException) = {
    trace(error)
    connection.stop()
  }

  def on_transport_disconnected = {}

  def on_transport_connected = {}

  def on_transport_command(command: AnyRef) = {}

}

object ProtocolFilter {
  def create_filters(clazzes:List[String], handler:ProtocolHandler) = {
    clazzes.map { clazz =>
      val filter = Broker.class_loader.loadClass(clazz).newInstance().asInstanceOf[ProtocolFilter]

      type ProtocolHandlerAware = { var protocol_handler:ProtocolHandler }
      try {
        filter.asInstanceOf[ProtocolHandlerAware].protocol_handler = handler
      } catch { case _ => }

      filter
    }
  }
}

trait ProtocolFilter {
  def filter[T](command: T):T
}
