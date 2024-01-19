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

import static io.wcm.testing.mock.aem.dam.MockAssetDelivery.PARAM_FORMAT;
import static io.wcm.testing.mock.aem.dam.MockAssetDelivery.PARAM_PATH;
import static io.wcm.testing.mock.aem.dam.MockAssetDelivery.PARAM_SEO_NAME;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.adobe.cq.wcm.spi.AssetDelivery;
import com.day.cq.dam.api.Asset;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockAssetDeliveryTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private AssetDelivery underTest;
  private Asset asset;
  private Resource assetResource;

  @Before
  public void setUp() throws Exception {
    this.underTest = context.registerInjectActivateService(MockAssetDelivery.class);
    asset = context.create().asset("/content/dam/test.jpg", 10, 10, "image/jpeg");
    assetResource = asset.adaptTo(Resource.class);
  }

  @Test
  public void testGetDeliveryURL() {
    String assetId = MockAssetDelivery.getAssetId(asset);
    assertWithParams("/adobe/dynamicmedia/deliver/" + assetId + "/test.jpg", Map.of());
    assertWithParams("/adobe/dynamicmedia/deliver/" + assetId + "/test.jpg?preferwebp=true&quality=80&width=100",
        Map.of("width", 100,
            "quality", 80,
            "preferwebp", true));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDeliveryURL_MissingMandatoryParam() {
    underTest.getDeliveryURL(assetResource, Map.of());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDeliveryURL_NullMap() {
    underTest.getDeliveryURL(assetResource, null);
  }

  private void assertWithParams(String expectedUrl, Map<String, Object> parameterMap) {
    Map<String, Object> allParams = new HashMap<>(parameterMap);
    allParams.put(PARAM_PATH, asset.getPath());
    allParams.put(PARAM_SEO_NAME, FilenameUtils.getBaseName(asset.getName()));
    allParams.put(PARAM_FORMAT, "jpg");
    String url = underTest.getDeliveryURL(assetResource, allParams);
    assertEquals(expectedUrl, url);
  }

}
