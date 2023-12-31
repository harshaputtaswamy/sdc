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
package org.openecomp.sdc.be.model;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterPropertyDataDefinition;

@ToString
@Getter
@Setter
public class ParsedToscaYamlInfo {

    Map<String, InputDefinition> inputs;
    Map<String, OutputDefinition> outputs;
    Map<String, UploadComponentInstanceInfo> instances;
    Map<String, GroupDefinition> groups;
    Map<String, PolicyDefinition> policies;
    Map<String, PropertyDefinition> properties;
    ListDataDefinition<SubstitutionFilterPropertyDataDefinition> substitutionFilterProperties;
    String substitutionMappingNodeType;
    Map<String, List<String>> substitutionMappingProperties;
}
