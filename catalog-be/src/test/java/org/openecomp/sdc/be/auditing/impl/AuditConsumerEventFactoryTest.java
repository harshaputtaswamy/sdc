/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.auditing.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

class AuditConsumerEventFactoryTest {

    private AuditConsumerEventFactory createTestSubject() {
        final CommonAuditData build = CommonAuditData.newBuilder().build();
        return new AuditConsumerEventFactory(AuditingActionEnum.ACTIVATE_SERVICE_BY_API, build, new User(), new ConsumerDefinition());
    }

    @Test
    void testGetDbEvent() throws Exception {
        AuditConsumerEventFactory testSubject;
        AuditingGenericEvent result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getDbEvent();
        Assertions.assertNotNull(result);
    }

    @Test
    void testGetLogMessage() throws Exception {
        AuditConsumerEventFactory testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getLogMessage();
        Assertions.assertNotNull(result);
    }

}
