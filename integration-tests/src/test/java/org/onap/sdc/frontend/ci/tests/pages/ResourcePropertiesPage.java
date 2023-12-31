/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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
 */

package org.onap.sdc.frontend.ci.tests.pages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Handles the Resource Properties Page UI actions
 */
public class ResourcePropertiesPage extends AbstractPageObject {

    public ResourcePropertiesPage(final WebDriver webDriver) {
        super(webDriver);
    }

    @Override
    public void isLoaded() {
        waitForElementVisibility(By.xpath(XpathSelector.TITLE_DIV.getXpath()));
        waitPropertiesToLoad();
    }

    /**
     * Waits for the properties table to load.
     */
    private void waitPropertiesToLoad() {
        waitForElementVisibility(By.xpath(XpathSelector.PROPERTIES_TABLE.getXpath()));
        waitForElementInvisibility(By.xpath(XpathSelector.NO_DATA_MESSAGE.getXpath()));
    }

    /**
     * Creates a map based on property names and data types
     */
    public Map<String, String> getPropertyNamesAndTypes() {
        waitPropertiesToLoad();
        final Map<String, String> namesAndTypes = new HashMap<>();
        final List<WebElement> names = findElements(By.xpath(XpathSelector.PROPERTY_NAMES.getXpath()));
        final List<WebElement> types = findElements(By.xpath(XpathSelector.PROPERTY_TYPES.getXpath()));

        for (int i = 0;i < names.size();i++) {
            namesAndTypes.put(names.get(i).getAttribute("innerText"), types.get(i).getAttribute("innerText"));
        }

        return namesAndTypes;
    }

    @AllArgsConstructor
    @Getter
    private enum XpathSelector {
        TITLE_DIV("//div[contains(@class,'tab-title') and contains(text(), 'Properties')]"),
        PROPERTIES_TABLE("//div[contains(@class,'table')]"),
        NO_DATA_MESSAGE("//div[contains(@class,'no-data') and text()='No data to display']"),
        PROPERTY_TYPES("//*[contains(@data-tests-id, 'propertyType')]"),
        PROPERTY_NAMES("//*[contains(@data-tests-id, 'propertyName')]");

        private final String xpath;
    }

}
