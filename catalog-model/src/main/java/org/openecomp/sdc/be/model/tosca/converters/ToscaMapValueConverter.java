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

package org.openecomp.sdc.be.model.tosca.converters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;

public class ToscaMapValueConverter extends ToscaValueBaseConverter implements ToscaValueConverter {

    private static final Logger log = Logger.getLogger(ToscaMapValueConverter.class.getName());
    private static final ToscaMapValueConverter mapConverter = new ToscaMapValueConverter();

    private ToscaMapValueConverter() {
    }

    public static ToscaMapValueConverter getInstance() {
        return mapConverter;
    }

    @Override
    public Object convertToToscaValue(String value, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        if (value == null) {
            return null;
        }
        try {
            ToscaPropertyType innerToscaType = ToscaPropertyType.isValidType(innerType);
            ToscaValueConverter innerConverter = null;
            boolean isScalar = true;
            List<PropertyDefinition> allPropertiesRecursive = new ArrayList<>();
            if (innerToscaType != null) {
                innerConverter = innerToscaType.getValueConverter();
            } else {
                DataTypeDefinition dataTypeDefinition = dataTypes.get(innerType);
                if (dataTypeDefinition != null) {
                    ToscaPropertyType toscaPropertyType;
                    if ((toscaPropertyType = isScalarType(dataTypeDefinition)) != null) {
                        innerConverter = toscaPropertyType.getValueConverter();
                    } else {
                        isScalar = false;
                        allPropertiesRecursive.addAll(dataTypeDefinition.getProperties());
                        DataTypeDefinition derivedFrom = dataTypeDefinition.getDerivedFrom();
                        while (!derivedFrom.getName().equals("tosca.datatypes.Root")) {
                            allPropertiesRecursive.addAll(derivedFrom.getProperties());
                            derivedFrom = derivedFrom.getDerivedFrom();
                        }
                    }
                } else {
                    log.debug("inner Tosca Type is null");
                    return value;
                }
            }
            JsonElement jsonElement;
            jsonElement = parseToJson(value);
            if (jsonElement == null || jsonElement.isJsonNull()) {
                log.debug("convertToToscaValue json element is null");
                return null;
            }
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
            Map<String, Object> toscaMap = new HashMap<>();
            final boolean isScalarF = isScalar;
            final ToscaValueConverter innerConverterFinal = innerConverter;
            entrySet.forEach(e -> {
                if (!e.getValue().isJsonNull()) {
                    convertEntry(innerType, dataTypes, allPropertiesRecursive, toscaMap, isScalarF, innerConverterFinal, e);
                }
            });
            return toscaMap;
        } catch (JsonParseException e) {
            log.debug("Failed to parse json : {}", value, e);
            BeEcompErrorManager.getInstance().logBeInvalidJsonInput("List Converter");
            return null;
        }
    }

    private void convertEntry(final String innerType, final Map<String, DataTypeDefinition> dataTypes,
                              final List<PropertyDefinition> allPropertiesRecursive, final Map<String, Object> toscaMap, final boolean isScalarF,
                              final ToscaValueConverter innerConverterFinal, final Entry<String, JsonElement> e) {
        log.debug("try convert element ");
        boolean scalar = isScalarF;
        String propType = innerType;
        ToscaValueConverter innerConverterProp = innerConverterFinal;
        if (!scalar) {
            for (PropertyDefinition pd : allPropertiesRecursive) {
                if (pd.getName().equals(e.getKey())) {
                    propType = pd.getType();
                    final DataTypeDefinition pdDataType = dataTypes.get(propType);
                    final ToscaPropertyType toscaPropType = isScalarType(pdDataType);
                    if (toscaPropType != null) {
                        scalar = true;
                        propType = toscaPropType.getType();
                        innerConverterProp = toscaPropType.getValueConverter();
                    }
                    break;
                }
            }
        }
        final Object convertedValue = convertDataTypeToToscaObject(propType, dataTypes, innerConverterProp, scalar, e.getValue(), false, false);
        toscaMap.put(e.getKey(), convertedValue);
    }

    public Object convertDataTypeToToscaObject(String innerType, Map<String, DataTypeDefinition> dataTypes, ToscaValueConverter innerConverter,
                                               final boolean isScalarF, JsonElement entryValue, boolean preserveEmptyValue,
                                               final boolean isToscaFunction) {
        Object convertedValue;
        if (isScalarF && isJsonElementAJsonPrimitive(entryValue)) {
            return innerConverter.convertToToscaValue(entryValue.getAsString(), innerType, dataTypes);
        } else {
            if (entryValue.isJsonPrimitive()) {
                return handleComplexJsonValue(entryValue);
            }
            // ticket 228696523 created   / DE272734 / Bug 154492 Fix
            if (entryValue instanceof JsonArray) {
                ArrayList<Object> toscaObjectPresentationArray = new ArrayList<>();
                JsonArray jsonArray = entryValue.getAsJsonArray();
                for (JsonElement jsonElement : jsonArray) {
                    Object convertedDataTypeToToscaMap = convertDataTypeToToscaMap(innerType, dataTypes, isScalarF, jsonElement, preserveEmptyValue,
                        isToscaFunction);
                    toscaObjectPresentationArray.add(convertedDataTypeToToscaMap);
                }
                convertedValue = toscaObjectPresentationArray;
            } else {
                convertedValue = convertDataTypeToToscaMap(innerType, dataTypes, isScalarF, entryValue, preserveEmptyValue, isToscaFunction);
            }
        }
        return convertedValue;
    }

    private Object convertDataTypeToToscaMap(String innerType, Map<String, DataTypeDefinition> dataTypes, final boolean isScalarF,
                                             JsonElement entryValue, boolean preserveEmptyValue, final boolean isToscaFunction) {
        Object convertedValue;
        if (entryValue.isJsonPrimitive()) {
            return json2JavaPrimitive(entryValue.getAsJsonPrimitive());
        }
        JsonObject asJsonObjectIn = entryValue.getAsJsonObject();
        if (!isToscaFunction) {
            DataTypePropertyConverter.getInstance()
                .mergeDataTypeDefaultValuesWithPropertyValue(asJsonObjectIn, innerType, dataTypes);
        }
        Map<String, Object> toscaObjectPresentation = new HashMap<>();
        Set<Entry<String, JsonElement>> entrySetIn = asJsonObjectIn.entrySet();
        for (Entry<String, JsonElement> entry : entrySetIn) {
            String propName = entry.getKey();
            JsonElement elementValue = entry.getValue();
            Object convValue;
            if (!isScalarF) {
                DataTypeDefinition dataTypeDefinition = dataTypes.get(innerType);
                Map<String, PropertyDefinition> allProperties = getAllProperties(dataTypeDefinition);
                PropertyDefinition propertyDefinition = allProperties.get(propName);
                if (propertyDefinition == null) {
                    log.trace("The property {} was not found under data type . Parse as map", propName);
                    if (elementValue.isJsonPrimitive()) {
                        convValue = elementValue.getAsJsonPrimitive();
                    } else {
                        convValue = handleComplexJsonValue(elementValue);
                    }
                } else {
                    String type = propertyDefinition.getType();
                    ToscaPropertyType propertyType = ToscaPropertyType.isValidType(type);
                    if (propertyType != null) {
                        if (isJsonElementAJsonPrimitive(elementValue)) {
                            ToscaValueConverter valueConverter = propertyType.getValueConverter();
                            convValue = valueConverter.convertToToscaValue(elementValue.getAsString(), type, dataTypes);
                        } else {
                            if (ToscaPropertyType.MAP.equals(propertyType) || ToscaPropertyType.LIST.equals(propertyType)) {
                                ToscaValueConverter valueConverter = propertyType.getValueConverter();
                                String json = gson.toJson(elementValue);
                                String innerTypeRecursive = propertyDefinition.getSchema().getProperty().getType();
                                convValue = valueConverter.convertToToscaValue(json, innerTypeRecursive, dataTypes);
                            } else {
                                convValue = handleComplexJsonValue(elementValue);
                            }
                        }
                    } else {
                        convValue = convertToToscaValue(elementValue.toString(), type, dataTypes);
                    }
                }
            } else {
                if (isJsonElementAJsonPrimitive(elementValue)) {
                    convValue = json2JavaPrimitive(elementValue.getAsJsonPrimitive());
                } else {
                    convValue = handleComplexJsonValue(elementValue);
                }
            }
            if (preserveEmptyValue || !isEmptyObjectValue(convValue) || isGetPolicyValue(propName)) {
                toscaObjectPresentation.put(propName, convValue);
            }
        }
        convertedValue = toscaObjectPresentation;
        return convertedValue;
    }

    private boolean isGetPolicyValue(String key) {
        return key.equals(ToscaFunctions.GET_POLICY.getFunctionName());
    }
}
