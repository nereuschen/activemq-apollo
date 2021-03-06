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
package org.apache.activemq.apollo.openwire.support.state;

import org.apache.activemq.apollo.openwire.command.BrokerInfo;
import org.apache.activemq.apollo.openwire.command.ConnectionControl;
import org.apache.activemq.apollo.openwire.command.ConnectionError;
import org.apache.activemq.apollo.openwire.command.ConnectionId;
import org.apache.activemq.apollo.openwire.command.ConnectionInfo;
import org.apache.activemq.apollo.openwire.command.ConsumerControl;
import org.apache.activemq.apollo.openwire.command.ConsumerId;
import org.apache.activemq.apollo.openwire.command.ConsumerInfo;
import org.apache.activemq.apollo.openwire.command.ControlCommand;
import org.apache.activemq.apollo.openwire.command.DestinationInfo;
import org.apache.activemq.apollo.openwire.command.FlushCommand;
import org.apache.activemq.apollo.openwire.command.KeepAliveInfo;
import org.apache.activemq.apollo.openwire.command.Message;
import org.apache.activemq.apollo.openwire.command.MessageAck;
import org.apache.activemq.apollo.openwire.command.MessageDispatch;
import org.apache.activemq.apollo.openwire.command.MessageDispatchNotification;
import org.apache.activemq.apollo.openwire.command.MessagePull;
import org.apache.activemq.apollo.openwire.command.ProducerAck;
import org.apache.activemq.apollo.openwire.command.ProducerId;
import org.apache.activemq.apollo.openwire.command.ProducerInfo;
import org.apache.activemq.apollo.openwire.command.RemoveInfo;
import org.apache.activemq.apollo.openwire.command.RemoveSubscriptionInfo;
import org.apache.activemq.apollo.openwire.command.Response;
import org.apache.activemq.apollo.openwire.command.SessionId;
import org.apache.activemq.apollo.openwire.command.SessionInfo;
import org.apache.activemq.apollo.openwire.command.ShutdownInfo;
import org.apache.activemq.apollo.openwire.command.TransactionInfo;
import org.apache.activemq.apollo.openwire.command.WireFormatInfo;

public class CommandVisitorAdapter implements CommandVisitor {

    public Response processAddConnection(ConnectionInfo info) throws Exception {
        return null;
    }

    public Response processAddConsumer(ConsumerInfo info) throws Exception {
        return null;
    }

    public Response processAddDestination(DestinationInfo info) throws Exception {
        return null;
    }

    public Response processAddProducer(ProducerInfo info) throws Exception {
        return null;
    }

    public Response processAddSession(SessionInfo info) throws Exception {
        return null;
    }

    public Response processBeginTransaction(TransactionInfo info) throws Exception {
        return null;
    }

    public Response processBrokerInfo(BrokerInfo info) throws Exception {
        return null;
    }

    public Response processCommitTransactionOnePhase(TransactionInfo info) throws Exception {
        return null;
    }

    public Response processCommitTransactionTwoPhase(TransactionInfo info) throws Exception {
        return null;
    }

    public Response processEndTransaction(TransactionInfo info) throws Exception {
        return null;
    }

    public Response processFlush(FlushCommand command) throws Exception {
        return null;
    }

    public Response processForgetTransaction(TransactionInfo info) throws Exception {
        return null;
    }

    public Response processKeepAlive(KeepAliveInfo info) throws Exception {
        return null;
    }

    public Response processMessage(Message send) throws Exception {
        return null;
    }

    public Response processMessageAck(MessageAck ack) throws Exception {
        return null;
    }

    public Response processMessageDispatchNotification(MessageDispatchNotification notification)
        throws Exception {
        return null;
    }

    public Response processMessagePull(MessagePull pull) throws Exception {
        return null;
    }

    public Response processPrepareTransaction(TransactionInfo info) throws Exception {
        return null;
    }

    public Response processProducerAck(ProducerAck ack) throws Exception {
        return null;
    }

    public Response processRecoverTransactions(TransactionInfo info) throws Exception {
        return null;
    }

    public Response processRemoveConnection(RemoveInfo info, ConnectionId id, long lastDeliveredSequenceId) throws Exception {
        return null;
    }

    public Response processRemoveConsumer(RemoveInfo info, ConsumerId id, long lastDeliveredSequenceId) throws Exception {
        return null;
    }

    public Response processRemoveDestination(DestinationInfo info) throws Exception {
        return null;
    }

    public Response processRemoveProducer(RemoveInfo info, ProducerId id) throws Exception {
        return null;
    }

    public Response processRemoveSession(RemoveInfo info, SessionId id, long lastDeliveredSequenceId) throws Exception {
        return null;
    }

    public Response processRemoveSubscription(RemoveSubscriptionInfo info) throws Exception {
        return null;
    }

    public Response processRollbackTransaction(TransactionInfo info) throws Exception {
        return null;
    }

    public Response processShutdown(ShutdownInfo info) throws Exception {
        return null;
    }

    public Response processWireFormat(WireFormatInfo info) throws Exception {
        return null;
    }

    public Response processMessageDispatch(MessageDispatch dispatch) throws Exception {
        return null;
    }

    public Response processControlCommand(ControlCommand command) throws Exception {
        return null;
    }

    public Response processConnectionControl(ConnectionControl control) throws Exception {
        return null;
    }

    public Response processConnectionError(ConnectionError error) throws Exception {
        return null;
    }

    public Response processConsumerControl(ConsumerControl control) throws Exception {
        return null;
    }

}
