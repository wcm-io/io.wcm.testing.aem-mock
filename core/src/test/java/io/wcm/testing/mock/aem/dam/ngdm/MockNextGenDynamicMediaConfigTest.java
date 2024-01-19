/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2024 wcm.io
 * %%
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
 * #L%
 */
package io.wcm.testing.mock.aem.dam.ngdm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockNextGenDynamicMediaConfigTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private MockNextGenDynamicMediaConfig underTest;

  @Before
  public void setUp() throws Exception {
    underTest = context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class);
  }

  @Test
  public void testDefaultProperties() {
    assertFalse(underTest.enabled());
    assertEquals(MockNextGenDynamicMediaConfig.DEFAULT_IMAGE_DELIVERY_BASE_PATH, underTest.getImageDeliveryBasePath());
  }

}
