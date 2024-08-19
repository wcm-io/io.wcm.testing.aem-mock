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
package io.wcm.testing.mock.aem.dam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.adobe.granite.asset.api.Asset;
import com.adobe.granite.asset.api.AssetManager;
import com.day.cq.commons.jcr.JcrConstants;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockGraniteAssetManagerWrapperTest {

  private static final String TEST_ASSET_PATH = "/content/dam/test-asset";

  @Rule
  public final AemContext context = TestAemContext.newAemContext();

  private ResourceResolver resourceResolver;
  private AssetManager assetManager;

  @Before
  public void setUp() {
    resourceResolver = context.resourceResolver();
    assetManager = context.graniteAssetManager();
  }

  @Test
  public void testBasicAssetFunctionality() {
    assertNull(resourceResolver.getResource(TEST_ASSET_PATH));
    assertFalse(assetManager.assetExists(TEST_ASSET_PATH));

    Asset asset = assetManager.createAsset(TEST_ASSET_PATH);
    assertNotNull(asset);
    assertEquals("dam:Asset", asset.getValueMap().get(JcrConstants.JCR_PRIMARYTYPE));
    assertTrue(assetManager.assetExists(TEST_ASSET_PATH));
    Resource assetResource = resourceResolver.getResource(TEST_ASSET_PATH);
    assertNotNull(assetResource);
    assertNotNull(assetResource.getChild(JcrConstants.JCR_CONTENT));
    assertNotNull(assetResource.getChild(JcrConstants.JCR_CONTENT + "/metadata"));
    assertNotNull(assetResource.getChild(JcrConstants.JCR_CONTENT + "/renditions"));

    assertEquals(asset, assetManager.getAsset(TEST_ASSET_PATH));

    assetManager.removeAsset(TEST_ASSET_PATH);
    assertNull(resourceResolver.getResource(TEST_ASSET_PATH));
    assertNull(assetManager.getAsset(TEST_ASSET_PATH));
    assertFalse(assetManager.assetExists(TEST_ASSET_PATH));
  }

}
