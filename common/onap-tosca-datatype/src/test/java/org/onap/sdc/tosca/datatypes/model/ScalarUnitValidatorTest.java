/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T. All rights reserved.
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

package org.onap.sdc.tosca.datatypes.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ScalarUnitValidatorTest {

    @Test
    public void shouldGetAnInstance() {
        assertNotNull(ScalarUnitValidator.getInstance());
    }

    @Test
    public void testScalarUnit() {
        ScalarUnitValidator validator = ScalarUnitValidator.getInstance();
        assertTrue(validator.isScalarUnit(null));
        assertTrue(validator.isScalarUnit("1y"));
        assertTrue(validator.isScalarUnit("2ye"));
        assertTrue(validator.isScalarUnit("3yes"));

        assertFalse(validator.isScalarUnit("no"));
        assertFalse(validator.isScalarUnit("1nooo"));
    }

    @Test
    public void testValueScalarUnit() {
        ScalarUnitValidator validator = ScalarUnitValidator.getInstance();
        assertTrue(validator.isValueScalarUnit("2B", TestEnum.class));

        assertFalse(validator.isValueScalarUnit("1val", TestEnum.class));
        assertFalse(validator.isValueScalarUnit("null", null));
        assertFalse(validator.isValueScalarUnit(null, null));
    }
}

enum TestEnum {
    B, KB
}