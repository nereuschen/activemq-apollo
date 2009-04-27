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
package org.apache.activemq.broker.store.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import java.io.File;

import org.apache.activemq.broker.store.Store;
import org.apache.activemq.broker.store.Store.QueueRecord;
import org.apache.activemq.protobuf.AsciiBuffer;
import org.apache.activemq.protobuf.Buffer;
import org.apache.activemq.queue.QueueStore;
import org.apache.activemq.queue.QueueStore.QueueDescriptor;
import org.apache.activemq.util.ByteArrayOutputStream;
import org.apache.activemq.util.ByteSequence;

/**
 * An in memory implementation of the {@link QueueStore} interface. It does not
 * properly roll back operations if an error occurs in the middle of a
 * transaction and it does not persist changes across restarts.
 */
public class MemoryStore implements Store {

    private MemorySession session = new MemorySession();
    private AtomicLong trackingGen = new AtomicLong(0);

    /**
     * @return a unique sequential store tracking number.
     */
    public long allocateStoreTracking() {
        return trackingGen.incrementAndGet();
    }

    static private class Stream {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private ByteSequence data;

        public void write(Buffer buffer) {
            if (baos == null) {
                throw new IllegalStateException("Stream closed.");
            }
            baos.write(buffer.data, buffer.offset, buffer.length);
        }

        public void close() {
            if (baos == null) {
                throw new IllegalStateException("Stream closed.");
            }
            data = baos.toByteSequence();
            baos = null;
        }

        public Buffer read(int offset, int max) {
            if (data == null) {
                throw new IllegalStateException("Stream not closed.");
            }
            if (offset > data.length) {
                // Invalid offset.
                return new Buffer(data.data, 0, 0);
            }
            offset += data.offset;
            max = Math.min(max, data.length - offset);
            return new Buffer(data.data, offset, max);
        }

    }

    static private class StoredQueue {
        QueueStore.QueueDescriptor descriptor;

        TreeMap<Long, QueueRecord> records = new TreeMap<Long, QueueRecord>();
        // Maps tracking to sequence number:
        HashMap<Long, Long> trackingMap = new HashMap<Long, Long>();
        int count = 0;
        long size = 0;
        HashMap<QueueStore.QueueDescriptor, StoredQueue> partitions;
        StoredQueue parent;

        StoredQueue(QueueStore.QueueDescriptor descriptor) {
            this.descriptor = descriptor.copy();
        }

        public void add(QueueRecord record) {
            records.put(record.getQueueKey(), record);
            trackingMap.put(record.getMessageKey(), record.getQueueKey());
            count++;
            size += record.getSize();
        }

        public boolean remove(Long msgKey) {
            Long sequenceKey = trackingMap.remove(msgKey);
            if (sequenceKey != null) {
                QueueRecord record = records.remove(sequenceKey);
                count--;
                size -= record.getSize();
                return true;
            }
            return false;
        }

        public Iterator<QueueRecord> list(Long firstQueueKey, long maxSequence, int max) {
            Collection<QueueRecord> list;
            if (max < 0) {
                list = new LinkedList<QueueRecord>();
            } else {
                list = new ArrayList<QueueRecord>(max);
            }
            
            for (Long key : records.tailMap(firstQueueKey).keySet()) {
                if ((max >= 0 && list.size() >= max) || (maxSequence >= 0 && key > maxSequence)) {
                    break;
                }
                list.add(records.get(key));
            }
            return list.iterator();
        }

        public int getCount() {
            return count;
        }

        public long getSize() {
            return size;
        }

        public void setParent(StoredQueue parent) {
            this.parent = parent;
            if (parent == null) {
                this.descriptor.setParent(null);
            } else {
                this.descriptor.setParent(parent.getQueueName());
            }
        }

        public StoredQueue getParent() {
            return parent;
        }

        public void addPartition(StoredQueue child) {
            if (partitions == null) {
                partitions = new HashMap<QueueStore.QueueDescriptor, StoredQueue>();
            }

            partitions.put(child.getDescriptor(), child);
        }

        public boolean removePartition(StoredQueue name) {
            if (partitions == null) {
                return false;
            }

            StoredQueue old = partitions.remove(name);
            if (old != null) {
                return true;
            }
            return false;
        }

        public Iterator<StoredQueue> getPartitions() {
            if (partitions == null) {
                return null;
            }

            return partitions.values().iterator();
        }

        public AsciiBuffer getQueueName() {
            return descriptor.getQueueName();
        }

        public QueueStore.QueueDescriptor getDescriptor() {
            return descriptor;
        }

        public QueueQueryResultImpl query() {
            QueueQueryResultImpl result = new QueueQueryResultImpl();
            result.count = count;
            result.size = size;
            result.firstSequence = records.isEmpty() ? 0 : records.firstEntry().getValue().getQueueKey();
            result.lastSequence = records.isEmpty() ? 0 : records.lastEntry().getValue().getQueueKey();
            result.desc = descriptor.copy();
            if (this.partitions != null) {
                ArrayList<QueueQueryResult> childResults = new ArrayList<QueueQueryResult>(partitions.size());
                for (StoredQueue child : partitions.values()) {
                    childResults.add(child.query());
                }
                result.partitions = childResults;
            }

            return result;
        }

    }

    static private class RemoveOp {
        QueueStore.QueueDescriptor queue;
        Long messageKey;

        public RemoveOp(QueueStore.QueueDescriptor queue, Long messageKey) {
            this.queue = queue;
            this.messageKey = messageKey;
        }
    }

    static private class Transaction {
        private ArrayList<Long> adds = new ArrayList<Long>(100);
        private ArrayList<RemoveOp> removes = new ArrayList<RemoveOp>(100);

        public void commit(MemorySession session) throws KeyNotFoundException {
            for (RemoveOp op : removes) {
                session.queueRemoveMessage(op.queue, op.messageKey);
            }
        }

        public void rollback(MemorySession session) {
            for (Long op : adds) {
                session.messageRemove(op);
            }
        }

        public void addMessage(Long messageKey) {
            adds.add(messageKey);
        }

        public void removeMessage(QueueStore.QueueDescriptor queue, Long messageKey) {
            removes.add(new RemoveOp(queue, messageKey));
        }
    }

    private static class MessageRecordHolder {
        final MessageRecord record;
        int refs = 0;

        public MessageRecordHolder(MessageRecord record) {
            this.record = record;
        }
    }

    private static class QueueQueryResultImpl implements QueueQueryResult {

        QueueStore.QueueDescriptor desc;
        Collection<QueueQueryResult> partitions;
        long size;
        int count;
        long firstSequence;
        long lastSequence;

        public QueueStore.QueueDescriptor getDescriptor() {
            return desc;
        }

        public Collection<QueueQueryResult> getPartitions() {
            return partitions;
        }

        public long getSize() {
            return size;
        }

        public int getCount() {
            return count;
        }

        public long getFirstSequence() {
            // TODO Auto-generated method stub
            return firstSequence;
        }

        public long getLastSequence() {
            // TODO Auto-generated method stub
            return lastSequence;
        }
    }

    private class MemorySession implements Session {

        long streamSequence;

        private HashMap<Long, MessageRecordHolder> messages = new HashMap<Long, MessageRecordHolder>();

        private TreeMap<AsciiBuffer, TreeMap<AsciiBuffer, Buffer>> maps = new TreeMap<AsciiBuffer, TreeMap<AsciiBuffer, Buffer>>();
        private TreeMap<Long, Stream> streams = new TreeMap<Long, Stream>();
        private TreeMap<AsciiBuffer, StoredQueue> queues = new TreeMap<AsciiBuffer, StoredQueue>();
        private TreeMap<Buffer, Transaction> transactions = new TreeMap<Buffer, Transaction>();

        // //////////////////////////////////////////////////////////////////////////////
        // Message related methods.
        // ///////////////////////////////////////////////////////////////////////////////
        public void messageAdd(MessageRecord record) {
            long key = record.getKey();
            if (key < 0) {
                throw new IllegalArgumentException("Key not set");
            }
            MessageRecordHolder holder = new MessageRecordHolder(record);
            MessageRecordHolder old = messages.put(key, holder);
            if (old != null) {
                messages.put(key, old);
            }
        }

        public void messageRemove(Long key) {
            messages.remove(key);
        }

        public MessageRecord messageGetRecord(Long key) {
            MessageRecordHolder holder = messages.get(key);
            if (holder != null) {
                return holder.record;
            }
            return null;
        }

        // //////////////////////////////////////////////////////////////////////////////
        // Queue related methods.
        // ///////////////////////////////////////////////////////////////////////////////
        public void queueAdd(QueueStore.QueueDescriptor desc) throws KeyNotFoundException {
            StoredQueue queue = queues.get(desc.getQueueName());
            if (queue == null) {
                queue = new StoredQueue(desc);
                // Add to the parent:
                AsciiBuffer parent = desc.getParent();

                // If the parent doesn't exist create it:
                if (parent != null) {
                    StoredQueue parentQueue = queues.get(parent);
                    if (parentQueue == null) {
                        throw new KeyNotFoundException("No parent " + parent + " for " + desc.getQueueName().toString());
                    }

                    parentQueue.addPartition(queue);
                    queue.setParent(parentQueue);
                }

                // Add the queue:
                queues.put(desc.getQueueName(), queue);
            }
        }

        public void queueRemove(QueueStore.QueueDescriptor desc) {
            StoredQueue queue = queues.get(desc.getQueueName());
            if (queue != null) {
                // Remove message references:
                for (QueueRecord record : queue.records.values()) {
                    deleteMessageReference(record.getMessageKey());
                }

                // Remove parent reference:
                StoredQueue parent = queue.getParent();
                if (parent != null) {
                    parent.removePartition(queue);
                }

                // Delete partitions
                Iterator<StoredQueue> partitions = queue.getPartitions();
                if (partitions != null) {
                    while (partitions.hasNext()) {
                        QueueStore.QueueDescriptor child = partitions.next().getDescriptor();
                        queueRemove(child);
                    }
                }

                // Remove the queue:
                queues.remove(desc.getQueueName());
            }
        }

        // Queue related methods.
        public Iterator<QueueQueryResult> queueListByType(short type, QueueStore.QueueDescriptor firstQueue, int max) {
            return queueListInternal(firstQueue, type, max);
        }

        public Iterator<QueueQueryResult> queueList(QueueStore.QueueDescriptor firstQueue, int max) {
            return queueListInternal(firstQueue, (short) -1, max);
        }

        private Iterator<QueueQueryResult> queueListInternal(QueueStore.QueueDescriptor firstQueue, short type, int max) {
            Collection<StoredQueue> tailResults;
            LinkedList<QueueQueryResult> results = new LinkedList<QueueQueryResult>();
            if (firstQueue == null) {
                tailResults = queues.values();
            } else {
                tailResults = queues.tailMap(firstQueue.getQueueName()).values();
            }

            for (StoredQueue sq : tailResults) {
                if (max >=0 && results.size() >= max) {
                    break;
                }
                if (type != -1 && sq.descriptor.getApplicationType() != type) {
                    continue;
                }
                results.add(sq.query());
            }

            return results.iterator();
        }

        public void queueAddMessage(QueueStore.QueueDescriptor queue, QueueRecord record) throws KeyNotFoundException {
            get(queues, queue.getQueueName()).add(record);
            MessageRecordHolder holder = messages.get(record.getMessageKey());
            if (holder != null) {
                holder.refs++;
            }
        }

        public void queueRemoveMessage(QueueStore.QueueDescriptor queue, Long msgKey) throws KeyNotFoundException {
            if (get(queues, queue.getQueueName()).remove(msgKey)) {
                deleteMessageReference(msgKey);
            }
        }

        private void deleteMessageReference(Long msgKey) {
            MessageRecordHolder holder = messages.get(msgKey);
            if (holder != null) {
                holder.refs--;
                if (holder.refs <= 0) {
                    messages.remove(msgKey);
                }
            }
        }

        public Iterator<QueueRecord> queueListMessagesQueue(QueueStore.QueueDescriptor queue, Long firstQueueKey, Long maxQueueKey, int max) throws KeyNotFoundException {
            return get(queues, queue.getQueueName()).list(firstQueueKey, maxQueueKey, max);
        }

        // //////////////////////////////////////////////////////////////////////////////
        // Simple Key Value related methods could come in handy to store misc
        // data.
        // ///////////////////////////////////////////////////////////////////////////////
        public boolean mapAdd(AsciiBuffer mapName) {
            if (maps.containsKey(mapName)) {
                return false;
            }
            maps.put(mapName, new TreeMap<AsciiBuffer, Buffer>());
            return true;
        }

        public boolean mapRemove(AsciiBuffer mapName) {
            return maps.remove(mapName) != null;
        }

        public Iterator<AsciiBuffer> mapList(AsciiBuffer first, int max) {
            return list(maps, first, max);
        }

        public Buffer mapEntryGet(AsciiBuffer mapName, AsciiBuffer key) throws KeyNotFoundException {
            return get(maps, mapName).get(key);
        }

        public Buffer mapEntryRemove(AsciiBuffer mapName, AsciiBuffer key) throws KeyNotFoundException {
            return get(maps, mapName).remove(key);
        }

        public Buffer mapEntryPut(AsciiBuffer mapName, AsciiBuffer key, Buffer value) throws KeyNotFoundException {
            return get(maps, mapName).put(key, value);
        }

        public Iterator<AsciiBuffer> mapEntryListKeys(AsciiBuffer mapName, AsciiBuffer first, int max) throws KeyNotFoundException {
            return list(get(maps, mapName), first, max);
        }

        // ///////////////////////////////////////////////////////////////////////////////
        // Stream related methods
        // ///////////////////////////////////////////////////////////////////////////////
        public Long streamOpen() {
            Long id = ++streamSequence;
            streams.put(id, new Stream());
            return id;
        }

        public void streamWrite(Long streamKey, Buffer buffer) throws KeyNotFoundException {
            get(streams, streamKey).write(buffer);
        }

        public void streamClose(Long streamKey) throws KeyNotFoundException {
            get(streams, streamKey).close();
        }

        public Buffer streamRead(Long streamKey, int offset, int max) throws KeyNotFoundException {
            return get(streams, streamKey).read(offset, max);
        }

        public boolean streamRemove(Long streamKey) {
            return streams.remove(streamKey) != null;
        }

        // ///////////////////////////////////////////////////////////////////////////////
        // Transaction related methods
        // ///////////////////////////////////////////////////////////////////////////////
        public void transactionAdd(Buffer txid) {
            transactions.put(txid, new Transaction());
        }

        public void transactionCommit(Buffer txid) throws KeyNotFoundException {
            remove(transactions, txid).commit(this);
        }

        public void transactionRollback(Buffer txid) throws KeyNotFoundException {
            remove(transactions, txid).rollback(this);
        }

        public Iterator<Buffer> transactionList(Buffer first, int max) {
            return list(transactions, first, max);
        }

        public void transactionAddMessage(Buffer txid, Long messageKey) throws KeyNotFoundException {
            get(transactions, txid).addMessage(messageKey);
            MessageRecordHolder holder = messages.get(messageKey);
            if (holder != null) {
                holder.refs++;
            }
        }

        public void transactionRemoveMessage(Buffer txid, QueueStore.QueueDescriptor queue, Long messageKey) throws KeyNotFoundException {
            get(transactions, txid).removeMessage(queue, messageKey);
            MessageRecordHolder holder = messages.get(messageKey);
            if (holder != null) {
                holder.refs--;
                if (holder.refs <= 0) {
                    messages.remove(messageKey);
                }
            }
        }
    }

    public void start() throws Exception {
    }

    public void stop() throws Exception {
    }

    public <R, T extends Exception> R execute(Callback<R, T> callback, Runnable runnable) throws T {
        R rc = callback.execute(session);
        if (runnable != null) {
            runnable.run();
        }
        return rc;
    }

    public void flush() {
    }

    static private <Key, Value> Iterator<Key> list(TreeMap<Key, Value> map, Key first, int max) {
        ArrayList<Key> rc = new ArrayList<Key>(max);
        Set<Key> keys = (first == null ? map : map.tailMap(first)).keySet();
        for (Key buffer : keys) {
            if (rc.size() >= max) {
                break;
            }
            rc.add(buffer);
        }
        return rc.iterator();
    }

    static private <Key, Value> Value get(TreeMap<Key, Value> map, Key key) throws KeyNotFoundException {
        Value value = map.get(key);
        if (value == null) {
            throw new KeyNotFoundException(key.toString());
        }
        return value;
    }

    static private <Key, Value> Value remove(TreeMap<Key, Value> map, Key key) throws KeyNotFoundException {
        Value value = map.remove(key);
        if (value == null) {
            throw new KeyNotFoundException(key.toString());
        }
        return value;
    }

    public void setStoreDirectory(File directory) {
        // NOOP
    }

    public void setDeleteAllMessages(boolean val) {
        // TODO Auto-generated method stub

    }

}