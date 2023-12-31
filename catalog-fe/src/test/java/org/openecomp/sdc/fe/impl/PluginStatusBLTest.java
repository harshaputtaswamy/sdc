/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.openecomp.sdc.fe.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.sdc.exception.InvalidArgumentException;
import org.openecomp.sdc.fe.config.ConfigurationManager;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class PluginStatusBLTest {


    @Mock
    private ConfigurationManager mockedConfigurationManager;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void validateBean() {
        assertThat(PluginStatusBL.class,  allOf(
                hasValidBeanConstructor()
        ));
    }
    @Test(expected = InvalidArgumentException.class)
    public void validateGetPluginsListWithNoConfigurationWillThrowException() {
        ConfigurationManager.setTestInstance(mockedConfigurationManager);
        when(mockedConfigurationManager.getPluginsConfiguration()).thenReturn(null);
        PluginStatusBL pluginStatusBL = new PluginStatusBL();

        pluginStatusBL.getPluginsList();
    }
    @Test(expected = InvalidArgumentException.class)
    public void validateGetPluginAvailabilityWithNoConfigurationWillThrowException() {
        ConfigurationManager.setTestInstance(mockedConfigurationManager);
        when(mockedConfigurationManager.getPluginsConfiguration()).thenReturn(null);
        PluginStatusBL pluginStatusBL = new PluginStatusBL();

        pluginStatusBL.getPluginAvailability("testPlugin");
    }
}
