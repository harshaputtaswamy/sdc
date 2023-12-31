/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.components.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fj.data.Either;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiServiceDataTransfer;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(locations = "classpath:paths/path-context.xml")
class ForwardingPathBusinessLogicTest extends BaseForwardingPathTest {

    @BeforeAll
    static void setup() {
        configurationManager =
                new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    void shouldFailToUpdateForwardingPathSincePathDoesNotExist() {
        Assertions.assertThrows(ComponentException.class, () -> {
            Service service = initForwardPath();
            bl.updateForwardingPath(FORWARDING_PATH_ID, service, user, true);
        });
    }

    @Test
    void shouldFailToDeleteForwardingPathSincePathDoesNotExist() {
        Assertions.assertThrows(ComponentException.class, () -> {
            initForwardPath();
            bl.deleteForwardingPaths("delete_forward_test", Sets.newHashSet(FORWARDING_PATH_ID), user, true);
        });
    }

    @Test
    void shouldSucceedCreateAndDeleteForwardingPath() {
        Service createdService = createService();
        Service service = initForwardPath();
        assertNotNull(service);
        Service serviceResponseFormatEither = bl.createForwardingPath(createdService.getUniqueId(), service, user, true);
        assertTrue(serviceResponseFormatEither != null);
        Map<String, ForwardingPathDataDefinition> forwardingPathsMap = serviceResponseFormatEither.getForwardingPaths();
        Set<String> pathIds = forwardingPathsMap.keySet();
        assertEquals(1, pathIds.size());
        String toscaResourceName = forwardingPathsMap.values().iterator().next().getToscaResourceName();

        // should return the created path
        Either<UiComponentDataTransfer, ResponseFormat> uiResaponse = bl.getComponentDataFilteredByParams(createdService.getUniqueId(), user,
                Lists.newArrayList(ComponentFieldsEnum.FORWARDING_PATHS.getValue()));
        assertTrue(uiResaponse.isLeft());
        UiServiceDataTransfer uiServiceDataTransfer = (UiServiceDataTransfer) uiResaponse.left().value();
        Map<String, ForwardingPathDataDefinition> forwardingPaths = uiServiceDataTransfer.getForwardingPaths();
        assertEquals(forwardingPaths.keySet(), pathIds);
        Map<String, ForwardingPathDataDefinition> updatedForwardingPaths = new HashMap<>(forwardingPaths);
        String newProtocol = "https";
        ForwardingPathDataDefinition forwardingPathDataDefinition = updatedForwardingPaths.values().stream().findAny().get();
        assertEquals(forwardingPathDataDefinition.getProtocol(), HTTP_PROTOCOL);
        assertEquals(toscaResourceName, forwardingPathDataDefinition.getToscaResourceName());
        ForwardingPathDataDefinition forwardingPathDataDefinitionUpdate = updatedForwardingPaths.values().iterator().next();
        // updated values
        forwardingPathDataDefinitionUpdate.setProtocol(newProtocol);
        forwardingPathDataDefinitionUpdate.setPathElements(new ListDataDefinition<>());

        // should update value
        service.getForwardingPaths().clear();
        service.getForwardingPaths().put(forwardingPathDataDefinitionUpdate.getUniqueId(), forwardingPathDataDefinitionUpdate);
        serviceResponseFormatEither = bl.updateForwardingPath(createdService.getUniqueId(), service, user, true);
        assertTrue(serviceResponseFormatEither != null);

        // make sure changes were applied
        uiResaponse = bl.getComponentDataFilteredByParams(createdService.getUniqueId(), user,
                Lists.newArrayList(ComponentFieldsEnum.FORWARDING_PATHS.getValue()));
        assertTrue(uiResaponse.isLeft());
        uiServiceDataTransfer = (UiServiceDataTransfer) uiResaponse.left().value();
        Map<String, ForwardingPathDataDefinition> forwardingPathsUpdated = uiServiceDataTransfer.getForwardingPaths();
        ForwardingPathDataDefinition updatedData = forwardingPathsUpdated.values().iterator().next();
        assertEquals(newProtocol, updatedData.getProtocol());
        assertTrue(updatedData.getPathElements().isEmpty());

        Service createdData = serviceResponseFormatEither;
        Set<String> paths = createdData.getForwardingPaths().keySet();
        Set<String> setResponseFormatEither = bl.deleteForwardingPaths(createdService.getUniqueId(), paths, user, true);
        assertTrue(setResponseFormatEither != null);

        // nothing to return now
        uiResaponse = bl.getComponentDataFilteredByParams(createdService.getUniqueId(), user,
                Lists.newArrayList(ComponentFieldsEnum.COMPONENT_INSTANCES.getValue(), ComponentFieldsEnum.FORWARDING_PATHS.getValue()));
        assertTrue(uiResaponse.isLeft());
        uiServiceDataTransfer = (UiServiceDataTransfer) uiResaponse.left().value();
        forwardingPaths = uiServiceDataTransfer.getForwardingPaths();
        assertTrue(forwardingPaths == null || forwardingPaths.isEmpty());

    }

}
