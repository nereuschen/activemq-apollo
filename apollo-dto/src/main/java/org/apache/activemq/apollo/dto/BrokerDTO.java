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
package org.apache.activemq.apollo.dto;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the root container for a broker's configuration.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@XmlRootElement(name="broker")
@XmlAccessorType(XmlAccessType.FIELD)
public class BrokerDTO {

    /**
     * Used to store any configuration notes.
     */
    @XmlElement
    public String notes;

    /**
     * A broker can service many virtual hosts.
     */
    @XmlElement(name="virtual_host")
    public List<VirtualHostDTO> virtual_hosts = new ArrayList<VirtualHostDTO>();

    /**
     * A broker accepts connections via it's configured connectors.
     */
    @XmlElement(name="connector")
    public List<ConnectorDTO> connectors = new ArrayList<ConnectorDTO>();

    /**
     * The clients should use to connect to this
     * broker.
     */
    @XmlElement(name="client_address")
    public String client_address;

    /**
     * The base data directory of the broker.  It will store
     * persistent data under it.
     */
    @XmlElementRef
    public KeyStorageDTO key_storage;

    @XmlElement(name="acl")
    public BrokerAclDTO acl;

    @XmlElement(name="web_admin")
    public List<WebAdminDTO> web_admins = new ArrayList<WebAdminDTO>();

    @XmlElement(name="authentication")
    public AuthenticationDTO authentication;

    @XmlElement(name="log_category")
    public LogCategoryDTO log_category;

    /**
     * Opaque service class names which gets started/stopped when the broker
     * starts/stops.
     */
    @XmlElement(name="service")
    public List<String> services = new ArrayList<String>();

    @Deprecated
    @XmlAttribute(name="sticky_dispatching")
    public Boolean sticky_dispatching;

    /**
     * If set to strict, then the broker will not start up if there
     * are any validation errors in the configuration file.
     */
    @XmlAttribute(name="validation")
    public String validation;
}
