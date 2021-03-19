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
package org.openecomp.sdc.be.info;

import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.model.ArtifactDefinition;

@Getter
@Setter
public class ArtifactDefinitionInfo {

    private String uniqueId;
    /**
     * Specifies the display name of the artifact.
     */
    private String artifactName;
    private String artifactDisplayName;
    private String artifactVersion;
    private String artifactUUID;

    public ArtifactDefinitionInfo(ArtifactDefinition artifactDefinition) {
        uniqueId = artifactDefinition.getUniqueId();
        artifactName = artifactDefinition.getArtifactName();
        artifactDisplayName = artifactDefinition.getArtifactDisplayName();
        artifactVersion = artifactDefinition.getArtifactVersion();
        artifactUUID = artifactDefinition.getArtifactUUID();
    }
}

