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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fj.data.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.distribution.engine.IDistributionEngine;
import org.openecomp.sdc.be.components.impl.ActivationRequestInformation;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.externalapi.servlet.representation.ServiceDistributionReqInfo;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.exception.ResponseFormat;

class ServiceDistributionValidationTest {

    private static final String USER_ID = "userId";
    private static final String SERVICE_ID = "serviceId";
    private static final String ENV_ID = "envId";

    @InjectMocks
    private ServiceDistributionValidation testInstance;

    @Mock
    private UserValidations userValidations;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private OperationalEnvironmentDao operationalEnvironmentDao;

    @Mock
    private IDistributionEngine distributionEngine;

    @Mock
    private ComponentsUtils componentsUtils;

    private User user;
    private Service service;
    private OperationalEnvironmentEntry operationalEnvironmentEntry;
    private ResponseFormat errResponse;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUserId(USER_ID);
        errResponse = new ResponseFormat();
        service = new Service();
        service.setUniqueId(SERVICE_ID);
        service.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        operationalEnvironmentEntry = new OperationalEnvironmentEntry();
        operationalEnvironmentEntry.setStatus(EnvironmentStatusEnum.COMPLETED);
    }

    @Test
    void validateActivateServiceRequest_userNotExist() {
        when(userValidations.validateUserExists(USER_ID)).thenThrow(new ByResponseFormatComponentException(errResponse));
        try {
            testInstance.validateActivateServiceRequest(SERVICE_ID, ENV_ID, user, new ServiceDistributionReqInfo("distributionData"));
        } catch (ByResponseFormatComponentException e) {
            assertEquals(errResponse, e.getResponseFormat());
        }
        verifyNoInteractions(toscaOperationFacade, operationalEnvironmentDao, componentsUtils);
    }

    @Test
    void validateActivateServiceRequest_ServiceNotExist() {
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(toscaOperationFacade.getLatestServiceByUuid(SERVICE_ID)).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR, ComponentTypeEnum.SERVICE)).thenReturn(
            ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.API_RESOURCE_NOT_FOUND, ApiResourceEnum.SERVICE_ID.getValue())).thenReturn(errResponse);
        Either<ActivationRequestInformation, ResponseFormat> activateServiceReq = testInstance.validateActivateServiceRequest(SERVICE_ID, ENV_ID,
            user, new ServiceDistributionReqInfo("distributionData"));
        assertEquals(errResponse, activateServiceReq.right().value());
        verifyNoInteractions(operationalEnvironmentDao);
    }

    @Test
    void validateActivateServiceRequest_ServiceLifeCycleStateNotReadyForDistribution() {
        service.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(toscaOperationFacade.getLatestServiceByUuid(SERVICE_ID)).thenReturn(Either.left(service));
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_SERVICE_STATE)).thenReturn(errResponse);
        Either<ActivationRequestInformation, ResponseFormat> activateServiceReq = testInstance.validateActivateServiceRequest(SERVICE_ID, ENV_ID,
            user, new ServiceDistributionReqInfo("distributionData"));
        assertEquals(errResponse, activateServiceReq.right().value());
        verifyNoInteractions(operationalEnvironmentDao);
    }

    @Test
    void validateActivateServiceRequest_operationalEnvNotExist() throws Exception {
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(toscaOperationFacade.getLatestServiceByUuid(SERVICE_ID)).thenReturn(Either.left(service));
        when(distributionEngine.getEnvironmentById(ENV_ID)).thenReturn(null);
        when(componentsUtils.getResponseFormat(eq(ActionStatus.API_RESOURCE_NOT_FOUND), anyString())).thenReturn(errResponse);
        Either<ActivationRequestInformation, ResponseFormat> activateServiceReq = testInstance.validateActivateServiceRequest(SERVICE_ID, ENV_ID,
            user, new ServiceDistributionReqInfo("distributionData"));
        assertEquals(errResponse, activateServiceReq.right().value());
    }

    @Test
    void validateActivateServiceRequest_operationalEnvStatusNotComplete() {
        operationalEnvironmentEntry.setStatus(EnvironmentStatusEnum.IN_PROGRESS);
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(toscaOperationFacade.getLatestServiceByUuid(SERVICE_ID)).thenReturn(Either.left(service));
        when(distributionEngine.getEnvironmentById(ENV_ID)).thenReturn(operationalEnvironmentEntry);
        when(componentsUtils.getResponseFormat(eq(ActionStatus.API_RESOURCE_NOT_FOUND), anyString())).thenReturn(errResponse);
        Either<ActivationRequestInformation, ResponseFormat> activateServiceReq = testInstance.validateActivateServiceRequest(SERVICE_ID, ENV_ID,
            user, new ServiceDistributionReqInfo("distributionData"));
        assertEquals(errResponse, activateServiceReq.right().value());
    }

    @Test
    void validateActivateServiceRequest_couldNotParseDistributionData() {
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(toscaOperationFacade.getLatestServiceByUuid(SERVICE_ID)).thenReturn(Either.left(service));
        when(distributionEngine.getEnvironmentById(ENV_ID)).thenReturn(operationalEnvironmentEntry);
        when(componentsUtils.getResponseFormat(ActionStatus.MISSING_BODY)).thenReturn(errResponse);
        Either<ActivationRequestInformation, ResponseFormat> activateServiceReq = testInstance.validateActivateServiceRequest(SERVICE_ID, ENV_ID,
            user, new ServiceDistributionReqInfo(null));
        assertEquals(errResponse, activateServiceReq.right().value());
    }

    @Test
    void validateActivateServiceRequest_distributionDataHasNoWorkloadContext() {
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(toscaOperationFacade.getLatestServiceByUuid(SERVICE_ID)).thenReturn(Either.left(service));
        when(distributionEngine.getEnvironmentById(ENV_ID)).thenReturn(operationalEnvironmentEntry);
        when(componentsUtils.getResponseFormat(ActionStatus.MISSING_BODY)).thenReturn(errResponse);
        Either<ActivationRequestInformation, ResponseFormat> activateServiceReq = testInstance.validateActivateServiceRequest(SERVICE_ID, ENV_ID,
            user, new ServiceDistributionReqInfo(""));
        assertEquals(errResponse, activateServiceReq.right().value());
    }

    @Test
    void validateActivateServiceRequest_requestValid() {
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(toscaOperationFacade.getLatestServiceByUuid(SERVICE_ID)).thenReturn(Either.left(service));
        when(distributionEngine.getEnvironmentById(ENV_ID)).thenReturn(operationalEnvironmentEntry);
        Either<ActivationRequestInformation, ResponseFormat> activateServiceReq = testInstance.validateActivateServiceRequest(SERVICE_ID, ENV_ID,
            user, new ServiceDistributionReqInfo("context"));
        assertEquals(service, activateServiceReq.left().value().getServiceToActivate());
        assertEquals("context", activateServiceReq.left().value().getWorkloadContext());
    }
}
