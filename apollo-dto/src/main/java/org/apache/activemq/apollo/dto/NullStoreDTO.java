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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Placeholder implementation fo a StoreDTO since JAXB
 * seems to fail with:
 * "Type <...> or any of its subclasses are not known to this context."
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@XmlRootElement(name="null_store")
@XmlAccessorType(XmlAccessType.FIELD)

public class NullStoreDTO extends StoreDTO {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NullStoreDTO)) return false;
        if (!super.equals(o)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 32;
    }
}
