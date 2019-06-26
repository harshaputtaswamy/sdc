/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.errors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class ImageErrorBuilderTest {

    @Test
    public void testGetDuplicateImageNameErrorBuilder() {
        //when
        ErrorCode errorCode = ImageErrorBuilder.getDuplicateImageNameErrorBuilder("1", "1");

        //then
        assertEquals(VendorSoftwareProductErrorCodes.DUPLICATE_IMAGE_NAME_NOT_ALLOWED, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Invalid request, Image with name 1 already exists for component with ID 1.", errorCode.message());

    }

    @Test
    public void testGetImageNameFormatErrorBuilder() {
        //when
        ErrorCode errorCode = ImageErrorBuilder.getImageNameFormatErrorBuilder("1");

        //then
        assertEquals(VendorSoftwareProductErrorCodes.IMAGE_NAME_FORMAT_NOT_ALLOWED, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Field does not conform to predefined criteria: name : must match 1", errorCode.message());

    }
}
