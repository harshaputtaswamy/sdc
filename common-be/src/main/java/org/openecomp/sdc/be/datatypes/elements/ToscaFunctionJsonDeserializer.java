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

package org.openecomp.sdc.be.datatypes.elements;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.yaml.snakeyaml.Yaml;

public class ToscaFunctionJsonDeserializer extends StdDeserializer<ToscaFunction> {

    public ToscaFunctionJsonDeserializer() {
        this(null);
    }

    public ToscaFunctionJsonDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ToscaFunction deserialize(final JsonParser jsonParser, final DeserializationContext context) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return deserializeToscaFunction(node, context);
    }

    private ToscaFunction deserializeToscaFunction(final JsonNode node, final DeserializationContext context) throws IOException {
        final String functionType;
        if (node.get("type") != null) {
            functionType = node.get("type").asText();
        } else if (node.get("functionType") != null) {
            //support for legacy tosca function
            functionType = node.get("functionType").asText();
        } else {
            throw context.instantiationException(ToscaFunction.class, "Attribute type not provided");
        }
        final ToscaFunctionType toscaFunctionType = ToscaFunctionType.findType(functionType)
            .orElseThrow(() -> context.instantiationException(ToscaFunction.class,
                String.format("Invalid function type '%s' or attribute type not provided", functionType))
            );
        if (toscaFunctionType == ToscaFunctionType.GET_INPUT || toscaFunctionType == ToscaFunctionType.GET_ATTRIBUTE
            || toscaFunctionType == ToscaFunctionType.GET_PROPERTY) {
            return deserializeToscaGetFunction(toscaFunctionType, node, context);
        }

        if (toscaFunctionType == ToscaFunctionType.CONCAT) {
            return this.deserializeConcatFunction(node, context);
        }

        if (toscaFunctionType == ToscaFunctionType.YAML) {
            return this.deserializeYamlFunction(node);
        }

        return null;
    }

    private ToscaFunction deserializeYamlFunction(JsonNode node) {
        var yamlFunction = new CustomYamlFunction();
        final Object value = new Yaml().load(node.get("value").asText());
        yamlFunction.setYamlValue(value);
        return yamlFunction;
    }

    private ToscaGetFunctionDataDefinition deserializeToscaGetFunction(final ToscaFunctionType toscaFunctionType, final JsonNode node,
                                                                       final DeserializationContext context) throws JsonMappingException {
        final ToscaGetFunctionDataDefinition toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(ToscaGetFunctionType.fromToscaFunctionType(toscaFunctionType).orElse(null));
        toscaGetFunction.setSourceName(node.get("sourceName").asText());
        toscaGetFunction.setPropertyUniqueId(node.get("propertyUniqueId").asText());
        final String propertySource = node.get("propertySource").asText();
        if (StringUtils.isNotEmpty(propertySource)) {
            final PropertySource propertySource1 = PropertySource.findType(propertySource).orElseThrow(() ->
                context.instantiationException(ToscaGetFunctionDataDefinition.class,
                    String.format("Invalid propertySource '%s'", propertySource))
            );
            toscaGetFunction.setPropertySource(propertySource1);
        }
        toscaGetFunction.setPropertyName(node.get("propertyName").asText());
        toscaGetFunction.setSourceName(node.get("sourceName").asText());
        toscaGetFunction.setSourceUniqueId(node.get("sourceUniqueId").asText());
        final JsonNode propertyPathFromSourceNode = node.get("propertyPathFromSource");
        if (propertyPathFromSourceNode != null) {
            if (!propertyPathFromSourceNode.isArray()) {
                throw context.instantiationException(ToscaGetFunctionDataDefinition.class, "Expecting an array for propertyPathFromSource attribute");
            }
            final List<String> pathFromSource = new ArrayList<>();
            propertyPathFromSourceNode.forEach(jsonNode -> pathFromSource.add(jsonNode.asText()));
            toscaGetFunction.setPropertyPathFromSource(pathFromSource);
        }

        return toscaGetFunction;
    }

    private ToscaConcatFunction deserializeConcatFunction(final JsonNode concatFunctionJsonNode,
                                                          final DeserializationContext context) throws IOException {
        final var toscaConcatFunction = new ToscaConcatFunction();
        final List<ToscaFunctionParameter> functionParameterList = new ArrayList<>();
        final JsonNode parametersNode = concatFunctionJsonNode.get("parameters");
        if (!parametersNode.isArray()) {
            throw context.instantiationException(List.class, "");
        }
        for (final JsonNode parameterNode : parametersNode) {
            final JsonNode typeJsonNode = parameterNode.get("type");
            if (typeJsonNode == null) {
                throw context.instantiationException(ToscaConcatFunction.class, "TOSCA concat function parameter type attribute not provided");
            }
            final String parameterType = typeJsonNode.asText();
            final ToscaFunctionType toscaFunctionType = ToscaFunctionType.findType(parameterType)
                .orElseThrow(() -> context.instantiationException(ToscaConcatFunction.class,
                    String.format("Invalid TOSCA concat function parameter type '%s'", parameterType))
                );
            if (toscaFunctionType == ToscaFunctionType.STRING) {
                final ToscaStringParameter toscaStringParameter = new ToscaStringParameter();
                toscaStringParameter.setValue(parameterNode.get("value").asText());
                functionParameterList.add(toscaStringParameter);
            } else {
                final ToscaFunction toscaFunction = this.deserializeToscaFunction(parameterNode, context);
                functionParameterList.add((ToscaFunctionParameter) toscaFunction);
            }
        }
        toscaConcatFunction.setParameters(functionParameterList);
        return toscaConcatFunction;
    }

}