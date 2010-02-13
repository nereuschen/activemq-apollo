/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * his work for additional information regarding copyright ownership.
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
package org.apache.activemq.amqp.protocol.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.String;
import java.util.Iterator;
import org.apache.activemq.amqp.protocol.marshaller.AmqpEncodingError;
import org.apache.activemq.amqp.protocol.marshaller.AmqpMarshaller;
import org.apache.activemq.amqp.protocol.marshaller.Encoded;
import org.apache.activemq.amqp.protocol.types.IAmqpList;
import org.apache.activemq.util.buffer.Buffer;

/**
 * Represents a the predicate to filter the Messages admitted onto the Link
 */
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
//!!!!!!!!THIS CLASS IS AUTOGENERATED DO NOT MODIFY DIRECTLY!!!!!!!!!!!!//
//!!!!!!Instead, modify the generator in activemq-amqp-generator!!!!!!!!//
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
public interface AmqpFilter extends AmqpList {



    /**
     * the type of the filter
     * <p>
     * Symbols are values from a constrained domain. Although the set of possible domains is
     * open-ended, typically the both number and size of symbols in use for any given application
     * will be small, e.g. small enough that it is reasonable to cache all the distinct values.
     * </p>
     */
    public void setFilterType(String filterType);

    /**
     * the type of the filter
     * <p>
     * Symbols are values from a constrained domain. Although the set of possible domains is
     * open-ended, typically the both number and size of symbols in use for any given application
     * will be small, e.g. small enough that it is reasonable to cache all the distinct values.
     * </p>
     */
    public void setFilterType(AmqpSymbol filterType);

    /**
     * the type of the filter
     * <p>
     * Symbols are values from a constrained domain. Although the set of possible domains is
     * open-ended, typically the both number and size of symbols in use for any given application
     * will be small, e.g. small enough that it is reasonable to cache all the distinct values.
     * </p>
     */
    public String getFilterType();

    /**
     * the filter predicate
     */
    public void setFilter(AmqpType<?, ?> filter);

    /**
     * the filter predicate
     */
    public AmqpType<?, ?> getFilter();

    public static class AmqpFilterBean implements AmqpFilter{

        private AmqpFilterBuffer buffer;
        private AmqpFilterBean bean = this;
        private AmqpSymbol filterType;
        private AmqpType<?, ?> filter;

        AmqpFilterBean() {
        }

        AmqpFilterBean(IAmqpList<AmqpType<?, ?>> value) {

            for(int i = 0; i < value.getListCount(); i++) {
                set(i, value.get(i));
            }
        }

        AmqpFilterBean(AmqpFilter.AmqpFilterBean other) {
            this.bean = other;
        }

        public final AmqpFilterBean copy() {
            return new AmqpFilter.AmqpFilterBean(bean);
        }

        public final AmqpFilter.AmqpFilterBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            if(buffer == null) {
                buffer = new AmqpFilterBuffer(marshaller.encode(this));
            }
            return buffer;
        }

        public final void marshal(DataOutput out, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError{
            getBuffer(marshaller).marshal(out, marshaller);
        }


        public void setFilterType(String filterType) {
            setFilterType(TypeFactory.createAmqpSymbol(filterType));
        }


        public final void setFilterType(AmqpSymbol filterType) {
            copyCheck();
            bean.filterType = filterType;
        }

        public final String getFilterType() {
            return bean.filterType.getValue();
        }

        public final void setFilter(AmqpType<?, ?> filter) {
            copyCheck();
            bean.filter = filter;
        }

        public final AmqpType<?, ?> getFilter() {
            return bean.filter;
        }

        public void set(int index, AmqpType<?, ?> value) {
            switch(index) {
            case 0: {
                setFilterType((AmqpSymbol) value);
                break;
            }
            case 1: {
                setFilter((AmqpType<?, ?>) value);
                break;
            }
            default : {
                throw new IndexOutOfBoundsException(String.valueOf(index));
            }
            }
        }

        public AmqpType<?, ?> get(int index) {
            switch(index) {
            case 0: {
                return bean.filterType;
            }
            case 1: {
                return bean.filter;
            }
            default : {
                throw new IndexOutOfBoundsException(String.valueOf(index));
            }
            }
        }

        public int getListCount() {
            return 2;
        }

        public IAmqpList<AmqpType<?, ?>> getValue() {
            return bean;
        }

        public Iterator<AmqpType<?, ?>> iterator() {
            return new AmqpListIterator<AmqpType<?, ?>>(bean);
        }


        private final void copyCheck() {
            if(buffer != null) {;
                throw new IllegalStateException("unwriteable");
            }
            if(bean != this) {;
                copy(bean);
            }
        }

        private final void copy(AmqpFilter.AmqpFilterBean other) {
            bean = this;
        }

        public boolean equals(Object o){
            if(this == o) {
                return true;
            }

            if(o == null || !(o instanceof AmqpFilter)) {
                return false;
            }

            return equals((AmqpFilter) o);
        }

        public boolean equals(AmqpFilter b) {

            if(b.getFilterType() == null ^ getFilterType() == null) {
                return false;
            }
            if(b.getFilterType() != null && !b.getFilterType().equals(getFilterType())){ 
                return false;
            }

            if(b.getFilter() == null ^ getFilter() == null) {
                return false;
            }
            if(b.getFilter() != null && !b.getFilter().equals(getFilter())){ 
                return false;
            }
            return true;
        }

        public int hashCode() {
            return AbstractAmqpList.hashCodeFor(this);
        }
    }

    public static class AmqpFilterBuffer extends AmqpList.AmqpListBuffer implements AmqpFilter{

        private AmqpFilterBean bean;

        protected AmqpFilterBuffer(Encoded<IAmqpList<AmqpType<?, ?>>> encoded) {
            super(encoded);
        }

        public void setFilterType(String filterType) {
            bean().setFilterType(filterType);
        }

        public final void setFilterType(AmqpSymbol filterType) {
            bean().setFilterType(filterType);
        }

        public final String getFilterType() {
            return bean().getFilterType();
        }

        public final void setFilter(AmqpType<?, ?> filter) {
            bean().setFilter(filter);
        }

        public final AmqpType<?, ?> getFilter() {
            return bean().getFilter();
        }

        public void set(int index, AmqpType<?, ?> value) {
            bean().set(index, value);
        }

        public AmqpType<?, ?> get(int index) {
            return bean().get(index);
        }

        public int getListCount() {
            return bean().getListCount();
        }

        public Iterator<AmqpType<?, ?>> iterator() {
            return bean().iterator();
        }

        public AmqpFilter.AmqpFilterBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            return this;
        }

        protected AmqpFilter bean() {
            if(bean == null) {
                bean = new AmqpFilter.AmqpFilterBean(encoded.getValue());
                bean.buffer = this;
            }
            return bean;
        }

        public boolean equals(Object o){
            return bean().equals(o);
        }

        public boolean equals(AmqpFilter o){
            return bean().equals(o);
        }

        public int hashCode() {
            return bean().hashCode();
        }

        public static AmqpFilter.AmqpFilterBuffer create(Encoded<IAmqpList<AmqpType<?, ?>>> encoded) {
            if(encoded.isNull()) {
                return null;
            }
            return new AmqpFilter.AmqpFilterBuffer(encoded);
        }

        public static AmqpFilter.AmqpFilterBuffer create(DataInput in, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError {
            return create(marshaller.unmarshalAmqpFilter(in));
        }

        public static AmqpFilter.AmqpFilterBuffer create(Buffer buffer, int offset, AmqpMarshaller marshaller) throws AmqpEncodingError {
            return create(marshaller.decodeAmqpFilter(buffer, offset));
        }
    }
}