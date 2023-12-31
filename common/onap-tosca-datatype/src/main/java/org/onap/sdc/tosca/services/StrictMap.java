/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.sdc.tosca.services;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StrictMap extends AbstractMap {

    private Map<Object, Object> delegate;

    public StrictMap(Map<Object, Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object put(Object key, Object value) {
        if (delegate == null) {
            delegate = new LinkedHashMap<>();
        }
        if (delegate.containsKey(key)) {
            throw new IllegalStateException("duplicate key: " + key);
        }
        return delegate.put(key, value);
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        if (delegate == null) {
            delegate = new LinkedHashMap<>();
        }
        return delegate.entrySet();
    }
}
