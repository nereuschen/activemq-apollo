package org.apache.activemq.broker;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.Connection;
import org.apache.activemq.flow.ISourceController;
import org.apache.activemq.metric.MetricAggregator;
import org.apache.activemq.metric.MetricCounter;
import org.apache.activemq.transport.DispatchableTransport;
import org.apache.activemq.transport.TransportFactory;

abstract public class RemoteConsumer extends Connection {

    protected final MetricCounter consumerRate = new MetricCounter();

    protected MetricAggregator totalConsumerRate;
    protected long thinkTime;
    protected Destination destination;
    protected String selector;
    protected URI uri;

    private boolean schedualWait;

    public void start() throws Exception {
        consumerRate.name("Consumer " + name + " Rate");
        totalConsumerRate.add(consumerRate);

        transport = TransportFactory.compositeConnect(uri);
        if(transport instanceof DispatchableTransport)
        {
            schedualWait = true;
        }
        initialize();
        super.start();
        setupSubscription();
        
    }

    
    abstract protected void setupSubscription() throws Exception;

    protected void messageReceived(final ISourceController<MessageDelivery> controller, final MessageDelivery elem) {
        if( schedualWait ) {
            if (thinkTime > 0) {
                getDispatcher().schedule(new Runnable(){
                    public void run() {
                        consumerRate.increment();
                        controller.elementDispatched(elem);
                    }
                }, thinkTime, TimeUnit.MILLISECONDS);
                
            }
            else
            {
                consumerRate.increment();
                controller.elementDispatched(elem);
            }

        } else {
            if( thinkTime>0 ) {
                try {
                    Thread.sleep(thinkTime);
                } catch (InterruptedException e) {
                }
            }
            consumerRate.increment();
            controller.elementDispatched(elem);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetricAggregator getTotalConsumerRate() {
        return totalConsumerRate;
    }

    public void setTotalConsumerRate(MetricAggregator totalConsumerRate) {
        this.totalConsumerRate = totalConsumerRate;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public long getThinkTime() {
        return thinkTime;
    }

    public void setThinkTime(long thinkTime) {
        this.thinkTime = thinkTime;
    }

    public MetricCounter getConsumerRate() {
        return consumerRate;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }}