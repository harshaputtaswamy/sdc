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
package org.openecomp.sdc.be.model.tosca.constraints;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintFunctionalException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.PropertyConstraintException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class LengthConstraint extends AbstractPropertyConstraint {

    @NotNull
    private Integer length;

    protected void doValidate(Object propertyValue) throws ConstraintViolationException {
        if (propertyValue instanceof String && String.valueOf(propertyValue).length() != length) {
            throw new ConstraintViolationException("The length of the value is not equals to [" + length + "]");
        } else if (propertyValue instanceof List && ((List) propertyValue).size() != length) {
            throw new ConstraintViolationException("The size of the list is not equals to [" + length + "]");
        } else if (propertyValue instanceof Map && ((Map) propertyValue).size() != length) {
            throw new ConstraintViolationException("The size of the map is not equals to [" + length + "]");
        }
    }

    public void validate(Object propertyValue) throws ConstraintViolationException {
        if (propertyValue == null) {
            throw new ConstraintViolationException("Value to validate is null");
        }
        if (!(propertyValue instanceof String || propertyValue instanceof List || propertyValue instanceof Map)) {
            throw new ConstraintViolationException("This constraint can only be applied on String/List/Map value");
        }
        doValidate(propertyValue);
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    @Override
    public ConstraintType getConstraintType() {
        return ConstraintType.LENGTH;
    }

    @Override
    public void validateValueOnUpdate(PropertyConstraint newConstraint) throws PropertyConstraintException {
    }

    @Override
    public String getErrorMessage(ToscaType toscaType, ConstraintFunctionalException e, String propertyName) {
        return getErrorMessage(toscaType, e, propertyName, "%s length must be [%s]", String.valueOf(length));
    }
}
