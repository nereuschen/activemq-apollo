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
package org.apache.activemq.apollo.broker.store.cassandra

import dto.CassandraStoreDTO
import org.apache.activemq.apollo.broker.store.StoreFactory
import org.apache.activemq.apollo.util._
import ReporterLevel._
import org.apache.activemq.apollo.dto.StoreDTO

/**
 * <p>
 * Hook to use a CassandraStore when a CassandraStoreDTO is
 * used in a broker configuration.
 * </p>
 * <p>
 * This class is discovered using the following resource file:
 * <code>META-INF/services/org.apache.activemq.apollo/stores</code>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class CassandraStoreFactory extends StoreFactory.Provider {

  def create(config: StoreDTO) = {
    config match {
      case config:CassandraStoreDTO =>
        new CassandraStore(config)
      case _ =>
        null
    }
  }

   def validate(config: StoreDTO, reporter:Reporter):ReporterLevel = {
     config match {
       case config:CassandraStoreDTO =>
         CassandraStore.validate(config, reporter)
       case _ =>
         null
     }
   }
}
