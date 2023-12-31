/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;

@Data
public class FilterConstraintDto {

    private String propertyName;
    private String capabilityName;
    private PropertyFilterTargetType targetType;
    private ConstraintType operator;
    private FilterValueType valueType;
    private Object value;
    private String originalType;

    public boolean isCapabilityPropertyFilter() {
        return capabilityName != null;
    }

    public Optional<ToscaGetFunctionDataDefinition> getAsToscaGetFunction() {
        if (value instanceof ToscaGetFunctionDataDefinition) {
            return Optional.of((ToscaGetFunctionDataDefinition) value);
        }
        try {
            return Optional.of(new ObjectMapper().convertValue(value, ToscaGetFunctionDataDefinition.class));
        } catch (final Exception ignored) {
            return Optional.empty();
        }
    }
    public Optional<List<ToscaGetFunctionDataDefinition>> getAsListToscaGetFunction() {
        List<ToscaGetFunctionDataDefinition> toscaGetFunctionDataDefinitionList = new ArrayList<>();
        if (value instanceof List) {
            try {
                ((List<?>) value).forEach(toscaValue -> toscaGetFunctionDataDefinitionList.add(new ObjectMapper().convertValue(toscaValue, ToscaGetFunctionDataDefinition.class)));
                return Optional.of(toscaGetFunctionDataDefinitionList);
            } catch (final Exception ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}

