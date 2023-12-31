/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 *
 *
 */
package org.openecomp.sdc.tosca.csar;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.IOException;

import static org.openecomp.sdc.tosca.csar.CSARConstants.ASD_DEFINITION_TYPE;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.ENTRY_DEFINITION_TYPE;

/**
 * Helper class for ASD packages.
 */
public class AsdPackageHelper {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AsdPackageHelper.class);

    private final ManifestUtils manifestUtils;

    public AsdPackageHelper(ManifestUtils manifestUtils) {
        this.manifestUtils = manifestUtils;
    }

    public boolean isAsdPackage(final FileContentHandler fileContentHandler) {
        try {
            final Manifest manifest = manifestUtils.loadManifest(fileContentHandler, new AsdManifestOnboarding());
            return null != manifest && manifest.getMetadata().entrySet().stream()
                    .anyMatch(manifestEntry -> ENTRY_DEFINITION_TYPE.getToken().equalsIgnoreCase(manifestEntry.getKey())
                            && ASD_DEFINITION_TYPE.equalsIgnoreCase(manifestEntry.getValue()));
        }
        catch (IOException ioe) {
            LOGGER.warn("There was a problem loading the manifest: ", ioe);
            return false;
        }
    }
}
