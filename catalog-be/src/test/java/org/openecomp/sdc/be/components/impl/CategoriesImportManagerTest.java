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

package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.category.MetadataKeyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.exception.ResponseFormat;

class CategoriesImportManagerTest {
    @InjectMocks
    static CategoriesImportManager importManager = new CategoriesImportManager();
    public static final IElementOperation elementOperation = Mockito.mock(IElementOperation.class);
    public static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    private static SubCategoryDefinition subcategory;

    @BeforeAll
    public static void beforeClass() throws IOException {
        subcategory = new SubCategoryDefinition();
        subcategory.setUniqueId("123");

        when(elementOperation.createCategory(Mockito.any(CategoryDefinition.class), Mockito.any(NodeTypeEnum.class))).thenAnswer((Answer<Either<CategoryDefinition, ActionStatus>>) invocation -> {
            Object[] args = invocation.getArguments();
            CategoryDefinition category = (CategoryDefinition) args[0];
            category.setUniqueId("123");
            return Either.left(category);
        });
        when(elementOperation.createSubCategory(Mockito.any(String.class), Mockito.any(SubCategoryDefinition.class), Mockito.any(NodeTypeEnum.class))).thenAnswer(new Answer<Either<SubCategoryDefinition, ActionStatus>>() {
            public Either<SubCategoryDefinition, ActionStatus> answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                // subcategory.setName(((SubCategoryDefinition)args[0]).getName());
                return Either.left(subcategory);
            }

        });

        // when(Mockito.any(SubCategoryDefinition.class).getUniqueId()).thenReturn("123");
    }

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void importCategoriesTest() throws IOException {
        String ymlContent = getYmlContent();
        Either<Map<String, List<CategoryDefinition>>, ResponseFormat> createCapabilityTypes = importManager.createCategories(ymlContent);
        
        assertTrue(createCapabilityTypes.isLeft());        
        final Map<String, List<CategoryDefinition>> categories = createCapabilityTypes.left().value();
        final Optional<CategoryDefinition> categoryVoIPCallControl = categories.get("services").stream().filter(category -> category.getName().equals("VoIP Call Control")).findAny();
        final Optional<CategoryDefinition> categoryWithServiceSubstitutionTrue = categories.get("services").stream().filter(category -> category.getName().equals("Category With Service Substitution True")).findAny();
        final Optional<CategoryDefinition> categoryWithServiceSubstitutionFalse = categories.get("services").stream().filter(category -> category.getName().equals("Category With Service Substitution False")).findAny();

        
        
        assertTrue(categoryVoIPCallControl.isPresent());
        assertFalse(categoryVoIPCallControl.get().isUseServiceSubstitutionForNestedServices());
        assertTrue(categoryWithServiceSubstitutionTrue.isPresent());
        assertTrue(categoryWithServiceSubstitutionTrue.get().isUseServiceSubstitutionForNestedServices());
        assertTrue(categoryWithServiceSubstitutionFalse.isPresent());
        assertFalse(categoryWithServiceSubstitutionFalse.get().isUseServiceSubstitutionForNestedServices());
    }

    @Test
    void categoriesNameAndDisplayNameTest() throws IOException {
        final String categoryName = "Category With DisplayName And metadata";
        final String expectedCategoryDisplayName = "Display Name For Category";
        final String ymlContent = getYmlContent();
        final Either<Map<String, List<CategoryDefinition>>, ResponseFormat> createCapabilityTypes = importManager.createCategories(ymlContent);
        final Map<String, List<CategoryDefinition>> categories = createCapabilityTypes.left().value();

        final Optional<CategoryDefinition> categoryWithNameAndDisplayName = categories.get("services").stream().filter(category -> category.getName().equals(categoryName)).findAny();
        final String categoryDisplayName = categoryWithNameAndDisplayName.get().getDisplayName();

        assertTrue(categoryWithNameAndDisplayName.isPresent());
        assertEquals(expectedCategoryDisplayName, categoryDisplayName);
    }

    @Test
    void getMetadataKeysTest() throws IOException {
        final String categoryName = "Category With DisplayName And metadata";
        final String expectedMetadataName = "ETSI Version";
        final String expectedEtsiVersion = "2.5.1";
        final String ymlContent = getYmlContent();
        final Either<Map<String, List<CategoryDefinition>>, ResponseFormat> createCapabilityTypes = importManager.createCategories(ymlContent);
        final Map<String, List<CategoryDefinition>> categories = createCapabilityTypes.left().value();

        final Optional<CategoryDefinition> categoryWithMetadata = categories.get("services").stream().filter(category -> category.getName().equals(categoryName)).findAny();
        final List<MetadataKeyDataDefinition> categoryMetadataList = categoryWithMetadata.get().getMetadataKeys();
        final MetadataKeyDataDefinition categoryMetadata = categoryMetadataList.get(0);

        assertEquals(expectedMetadataName, categoryMetadata.getName());
        assertEquals(expectedEtsiVersion, categoryMetadata.getValidValues().get(0));
        assertEquals(expectedEtsiVersion, categoryMetadata.getDefaultValue());
        assertTrue(categoryMetadata.isMandatory());
    }

    private String getYmlContent() throws IOException {
        Path filePath = Paths.get("src/test/resources/types/categoryTypes.yml");
        byte[] fileContent = Files.readAllBytes(filePath);
        return new String(fileContent);
    }
}
