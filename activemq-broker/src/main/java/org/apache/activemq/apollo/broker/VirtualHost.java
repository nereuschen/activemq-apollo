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
package org.apache.activemq.apollo.broker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.activemq.Service;
import org.apache.activemq.apollo.broker.ProtocolHandler.ConsumerContext;
import org.apache.activemq.broker.store.Store;
import org.apache.activemq.broker.store.StoreFactory;
import org.apache.activemq.protobuf.AsciiBuffer;
import org.apache.activemq.queue.AbstractFlowQueue;
import org.apache.activemq.queue.ExclusivePersistentQueue;
import org.apache.activemq.queue.IQueue;

/**
 * @author chirino
 */
public class VirtualHost implements Service {

    final private BrokerQueueStore queueStore = new BrokerQueueStore();
    final private HashMap<AsciiBuffer, Queue> queues = new HashMap<AsciiBuffer, Queue>();
    final private HashMap<String, DurableSubscription> durableSubs = new HashMap<String, DurableSubscription>();
    final private Router router = new Router();
    
    private ArrayList<AsciiBuffer> hostNames = new ArrayList<AsciiBuffer>();
    private Broker broker;
    private boolean started;
    private BrokerDatabase database;

    public VirtualHost() {
        this.router.setVirtualHost(this);
    }

    public VirtualHost(String name) {
    	this();
    	addHostName(new AsciiBuffer(name));
	}

	public AsciiBuffer getHostName() {
        if (hostNames.size() > 0) {
            hostNames.get(0);
        }
        return null;
    }

    public List<AsciiBuffer> getHostNames() {
        return hostNames;
    }
    public void setHostNames(List<AsciiBuffer> hostNames) {
        this.hostNames = new ArrayList<AsciiBuffer>(hostNames);
    }
    public void addHostName(AsciiBuffer hostName) {
        this.hostNames.add(hostName);
    }
    public void removeHostName(AsciiBuffer hostName) {
        this.hostNames.remove(hostName);
    }

    public Router getRouter() {
        return router;
    }
    
    public BrokerDatabase getDatabase() {
        return database;
    }
    public void setDatabase(BrokerDatabase database) {
        this.database = database;
    }
    public void setStore(Store store) {
        database = new BrokerDatabase(store);
    }

    public synchronized void start() throws Exception {

        if (started) {
            return;
        }

		if ( database == null ) {
			Store store = StoreFactory.createStore("kaha-db");
			database = new BrokerDatabase(store);
		}
	    database.setDispatcher(broker.getDispatcher());
	    database.start();

        router.setDatabase(database);
        queueStore.setDatabase(database);
        queueStore.setDispatcher(broker.getDispatcher());
        queueStore.loadQueues();
        // Create Queue instances
        for (IQueue<Long, MessageDelivery> iQueue : queueStore.getSharedQueues()) {
            Queue queue = new Queue(iQueue);
            Domain domain = router.getDomain(Router.QUEUE_DOMAIN);
            Destination dest = new Destination.SingleDestination(Router.QUEUE_DOMAIN, iQueue.getDescriptor().getQueueName());
            queue.setDestination(dest);
            domain.add(dest.getName(), queue);
            queues.put(dest.getName(), queue);
        }
        for (Queue queue : queues.values()) {
            queue.start();
        }
        started = true;
    }

    public synchronized void stop() throws Exception {
        if (!started) {
            return;
        }
        for (Queue queue : queues.values()) {
            queue.shutdown(true);
        }

        for (AbstractFlowQueue<MessageDelivery> queue : queueStore.getDurableQueues()) {
            queue.shutdown(true);
        }
        
        database.stop();
        started = false;
    }

    public synchronized Queue createQueue(Destination dest) throws Exception {
        if (!started) {
            //Queues from the store must be loaded before we can create new ones:
            throw new IllegalStateException("Can't create queue on unstarted host");
        }

        Queue queue = queues.get(dest);
        // If the queue doesn't exist create it:
        if (queue == null) {
            IQueue<Long, MessageDelivery> iQueue = queueStore.createSharedQueue(dest.getName().toString());
            queue = new Queue(iQueue);
            queue.setDestination(dest);
            Domain domain = router.getDomain(Router.QUEUE_DOMAIN);
            domain.add(dest.getName(), queue);
            queues.put(dest.getName(), queue);
        }
        queue.start();
        return queue;
    }

    public BrokerQueueStore getQueueStore() {
        return queueStore;
    }

    public BrokerSubscription createSubscription(ConsumerContext consumer) {
        Destination destination = consumer.getDestination();
        BrokerSubscription sub = null;

        if (consumer.isDurable()) {
            DurableSubscription dsub = durableSubs.get(consumer.getSubscriptionName());
            if (dsub == null) {
                ExclusivePersistentQueue<Long, MessageDelivery> queue = queueStore.createDurableQueue(consumer.getSubscriptionName());
                queue.start();
                dsub = new DurableSubscription(this, destination, consumer.getSelectorExpression(), queue);
                durableSubs.put(consumer.getSubscriptionName(), dsub);
            }
            sub = dsub;
        } else {
            if(destination.getDestinations() != null)
            {
                sub = new MultiSubscription(this, destination, consumer.getSelectorExpression());
            }
            else
            {
                if (destination.getDomain().equals(Router.TOPIC_DOMAIN)) {
                    sub = new TopicSubscription(this, destination, consumer.getSelectorExpression());
                } else {
                    Queue queue = queues.get(destination.getName());
                    sub = new Queue.QueueSubscription(queue);
                }
            }
        }
        return sub;
    }

	public Broker getBroker() {
		return broker;
	}

	public void setBroker(Broker broker) {
		this.broker = broker;
	}
}