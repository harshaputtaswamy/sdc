/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import fj.data.Either;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.exception.ResponseFormat;

class ServiceValidationsTest extends ServiceBusinessLogicBaseTestSetup {

    @Test
    void testInvalidEnvironmentContext() {
        Service newService = createServiceObject(false);
        newService.setEnvironmentContext("not valid");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_ENVIRONMENT_CONTEXT, "not valid");
            return;
        }
        fail();
    }

    @Test
    void testValidEnvironmentContext() {
        Service newService = createServiceObject(false);
        newService.setEnvironmentContext("Critical_Revenue-Bearing");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getEnvironmentContext()).isEqualTo("Critical_Revenue-Bearing");
    }

    @Test
    void testCreateServiceWithNoEnvironmentContext() {
        Service newService = createServiceObject(false);
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getEnvironmentContext()).isEqualTo("General_Revenue-Bearing");
    }

    @Test
    void testInvalidInstantiationType() {
        Service newService = createServiceObject(false);
        newService.setInstantiationType("not valid");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_INSTANTIATION_TYPE, "not valid");
            return;
        }
        fail();
    }

    @Test
    void testEmptyInstantiationType() {
        Service newService = createServiceObject(false);
        newService.setInstantiationType(null);
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getInstantiationType()).isEqualTo("A-la-carte");
    }

    @Test
    void testValidInstantiationType() {
        Service newService = createServiceObject(false);
        newService.setInstantiationType("Macro");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getInstantiationType()).isEqualTo("Macro");
    }

    @Test
    void testInvalidServiceRole() {
        Service newService = createServiceObject(false);
        newService.setServiceRole("gsg*");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_PROPERY, SERVICE_ROLE);
            return;
        }
        fail();
    }

    @Test
    void testTooLongServiceRole() {
        Service newService = createServiceObject(false);
        newService.setServiceRole("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.PROPERTY_EXCEEDS_LIMIT, SERVICE_ROLE);
            return;
        }
        fail();
    }

    @Test
    void testValidServiceRole() {
        Service newService = createServiceObject(false);
        newService.setServiceRole("role");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getServiceRole()).isEqualTo("role");
    }

    @Test
    void testInvalidServiceType() {
        Service newService = createServiceObject(false);
        newService.setServiceType("gsg*");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_PROPERY, SERVICE_TYPE);
            return;
        }
        fail();
    }

    @Test
    void testValidServiceType() {
        Service newService = createServiceObject(false);
        newService.setServiceType("type");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getServiceType()).isEqualTo("type");
    }

    @Test
    void testTooLongServiceType() {
        Service newService = createServiceObject(false);
        newService.setServiceType("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.PROPERTY_EXCEEDS_LIMIT, SERVICE_TYPE);
            return;
        }
        fail();
    }

    @Test
    void testEcompGeneratedNamingIsMissing() {
        Service newService = createServiceObject(false);
        newService.setEcompGeneratedNaming(null);
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.MISSING_ECOMP_GENERATED_NAMING);
            return;
        }
        fail();

    }

    @Test
    void testNamingPolicyWIthEcompNamingFalse() {
        Service newService = createServiceObject(false);
        newService.setEcompGeneratedNaming(false);
        newService.setNamingPolicy("policy");
        Either<Service, ResponseFormat> service = bl.validateServiceBeforeCreate(newService, user, AuditingActionEnum.CREATE_RESOURCE);
        assertThat(service.left().value().getNamingPolicy()).isEqualTo("");
    }

    @Test
    void testInvalidNamingPolicy() {
        Service newService = createServiceObject(false);
        newService.setNamingPolicy("פוליסי");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.INVALID_NAMING_POLICY);
            return;
        }
        fail();
    }

    @Test
    void testTooLongNamingPolicy() {
        Service newService = createServiceObject(false);
        newService.setNamingPolicy("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        try {
            bl.createService(newService, user);
        } catch (ComponentException exp) {
            assertComponentException(exp, ActionStatus.NAMING_POLICY_EXCEEDS_LIMIT, "100");
            return;
        }
        fail();
    }
}
