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

import static io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig.DEFAULT_ASSET_METADATA_PATH;
import static io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig.DEFAULT_ASSET_ORIGINAL_BINARY_DELIVERY_PATH;
import static io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig.DEFAULT_ASSET_SELECTORS_JS_URL;
import static io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig.DEFAULT_ENV;
import static io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig.DEFAULT_IMAGE_DELIVERY_BASE_PATH;
import static io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig.DEFAULT_IMS_ENV;
import static io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig.DEFAULT_VIDEO_DELIVERY_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    assertNull(underTest.getRepositoryId());
    assertNull(underTest.getApiKey());
    assertEquals(DEFAULT_ENV, underTest.getEnv());
    assertNull(underTest.getImsOrg());
    assertEquals(DEFAULT_IMS_ENV, underTest.getImsEnv());
    assertNull(underTest.getImsClient());
    assertEquals(DEFAULT_ASSET_SELECTORS_JS_URL, underTest.getAssetSelectorsJsUrl());
    assertEquals(DEFAULT_IMAGE_DELIVERY_BASE_PATH, underTest.getImageDeliveryBasePath());
    assertEquals(DEFAULT_VIDEO_DELIVERY_PATH, underTest.getVideoDeliveryPath());
    assertEquals(DEFAULT_ASSET_ORIGINAL_BINARY_DELIVERY_PATH, underTest.getAssetOriginalBinaryDeliveryPath());
    assertEquals(DEFAULT_ASSET_METADATA_PATH, underTest.getAssetMetadataPath());
  }

  @Test
  public void testProperties() {
    underTest.setEnabled(true);
    underTest.setRepositoryId("repository1");
    underTest.setApiKey("key1");
    underTest.setEnv("STAGE");
    underTest.setImsOrg("org1");
    underTest.setImsEnv("stg1");
    underTest.setImsClient("client1");
    underTest.setAssetSelectorsJsUrl("https://selectors1");
    underTest.setImageDeliveryBasePath("/image1");
    underTest.setVideoDeliveryPath("/video1");
    underTest.setAssetOriginalBinaryDeliveryPath("/original1");
    underTest.setAssetMetadataPath("/metadata1");

    assertTrue(underTest.enabled());
    assertEquals("repository1", underTest.getRepositoryId());
    assertEquals("key1", underTest.getApiKey());
    assertEquals("STAGE", underTest.getEnv());
    assertEquals("org1", underTest.getImsOrg());
    assertEquals("stg1", underTest.getImsEnv());
    assertEquals("client1", underTest.getImsClient());
    assertEquals("https://selectors1", underTest.getAssetSelectorsJsUrl());
    assertEquals("/image1", underTest.getImageDeliveryBasePath());
    assertEquals("/video1", underTest.getVideoDeliveryPath());
    assertEquals("/original1", underTest.getAssetOriginalBinaryDeliveryPath());
    assertEquals("/metadata1", underTest.getAssetMetadataPath());
  }

}
