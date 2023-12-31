/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.core.model.dao;

import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface ServiceModelDao<M> extends VersionableDao {

    M getServiceModel(String vspId, Version version);

    /**
     * Store a service model.
     *
     * @param vspId        the Vendor Software Product id
     * @param version      the Vendor Software Product version
     * @param serviceModel the service model to store
     */
    void storeServiceModel(String vspId, Version version, M serviceModel);

    void deleteAll(String vspId, Version version);

    /**
     * This method gets used in healing, in order to replace the healed service model with the existing one without creating any conflicts
     **/
    void overrideServiceModel(String vspId, Version version, M serviceModel);
}
