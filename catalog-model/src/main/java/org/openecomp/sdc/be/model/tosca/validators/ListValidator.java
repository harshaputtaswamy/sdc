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
package org.openecomp.sdc.be.model.tosca.validators;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.Map;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.JsonUtils;

public class ListValidator implements PropertyTypeValidator {

    private static final Logger log = Logger.getLogger(ListValidator.class.getName());
    private static ListValidator listValidator = new ListValidator();
    private static JsonParser jsonParser = new JsonParser();
    private static DataTypeValidatorConverter dataTypeValidatorConverter = DataTypeValidatorConverter.getInstance();

    public static ListValidator getInstance() {
        return listValidator;
    }

    @Override
    public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {
        log.debug("Going to validate value {} with inner type {}", value, innerType);
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }
        if (innerType == null) {
            return false;
        }
        PropertyTypeValidator innerValidator;
        ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(innerType);
        if (innerToscaType != null) {
            switch (innerToscaType) {
                case STRING:
                    innerValidator = ToscaPropertyType.STRING.getValidator();
                    break;
                case INTEGER:
                    innerValidator = ToscaPropertyType.INTEGER.getValidator();
                    break;
                case FLOAT:
                    innerValidator = ToscaPropertyType.FLOAT.getValidator();
                    break;
                case BOOLEAN:
                    innerValidator = ToscaPropertyType.BOOLEAN.getValidator();
                    break;
                case JSON:
                    innerValidator = ToscaPropertyType.JSON.getValidator();
                    break;
                case MAP:
                	innerValidator = ToscaPropertyType.MAP.getValidator();
                	break;
                case RANGE:
                    innerValidator = ToscaPropertyType.RANGE.getValidator();
                    break;
                case SCALAR_UNIT_BITRATE:
                    innerValidator = ToscaPropertyType.SCALAR_UNIT_BITRATE.getValidator();
                    break;
                case SCALAR_UNIT_FREQUENCY:
                    innerValidator = ToscaPropertyType.SCALAR_UNIT_FREQUENCY.getValidator();
                    break;
                case SCALAR_UNIT_TIME:
                    innerValidator = ToscaPropertyType.SCALAR_UNIT_TIME.getValidator();
                    break;
                case SCALAR_UNIT_SIZE:
                    innerValidator = ToscaPropertyType.SCALAR_UNIT_SIZE.getValidator();
                    break;
                case SCALAR_UNIT:
                    innerValidator = ToscaPropertyType.SCALAR_UNIT.getValidator();
                    break;
                case TIMESTAMP:
                    innerValidator = ToscaPropertyType.TIMESTAMP.getValidator();
                    break;
                default:
                    log.debug("inner Tosca Type is unknown. {}", innerToscaType);
                    return false;
            }
        } else {
            log.debug("inner Tosca Type is: {}", innerType);
            boolean isValid = validateComplexInnerType(value, innerType, allDataTypes);
            log.debug("Finish to validate value {} of list with inner type {}. result is {}", value, innerType, isValid);
            return isValid;
        }
        try {
            JsonArray jo = (JsonArray) jsonParser.parse(value);
            if (ToscaPropertyType.JSON == innerToscaType) {
                return true;
            }
            int size = jo.size();
            for (int i = 0; i < size; i++) {
                JsonElement currentValue = jo.get(i);
                String element = JsonUtils.toString(currentValue);
                if (!innerValidator.isValid(element, innerToscaType.equals(ToscaPropertyType.MAP)? ToscaPropertyType.STRING.getType(): null, allDataTypes)) {
                    log.debug("validation of element : {} failed", element);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.debug("Failed to parse json : {}", value, e);
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput("List Validator");
        }
        return false;
    }

    @Override
    public boolean isValid(String value, String innerType) {
        return isValid(value, innerType, null);
    }

    private boolean validateComplexInnerType(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {
        DataTypeDefinition innerDataTypeDefinition = allDataTypes.get(innerType);
        if (innerDataTypeDefinition == null) {
            log.debug("Data type {} cannot be found in our data types.", innerType);
            return false;
        }
        try {
            JsonArray jo = (JsonArray) jsonParser.parse(value);
            int size = jo.size();
            for (int i = 0; i < size; i++) {
                JsonElement currentValue = jo.get(i);
                if (currentValue != null) {
                    String element = JsonUtils.toString(currentValue);
                    boolean isValid = dataTypeValidatorConverter.isValid(element, innerDataTypeDefinition, allDataTypes);
                    if (!isValid) {
                        log.debug("Cannot parse value {} from type {} in list parameter", currentValue, innerType);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error when parsing JSON of object of type ", e);
            return false;
        }
        return true;
    }
}
