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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.service.event.EventHandler;

import com.adobe.granite.asset.api.Asset;
import com.adobe.granite.asset.api.Rendition;
import com.day.cq.dam.api.DamEvent;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockGraniteAssetWrapperTest {

  private static final byte[] BINARY_DATA = new byte[] {
      0x01, 0x02, 0x03, 0x04, 0x05
  };

  @Rule
  public final AemContext context = TestAemContext.newAemContext();

  private Asset asset;
  private MockAssetManagerTest.DamEventHandler damEventHandler;

  @Before
  @SuppressWarnings("null")
  public void setUp() {
    context.load().json("/json-import-samples/dam.json", "/content/dam/sample");

    Resource resource = this.context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg");
    this.asset = resource.adaptTo(Asset.class);

    this.damEventHandler = (MockAssetManagerTest.DamEventHandler)context.registerService(EventHandler.class, new MockAssetManagerTest.DamEventHandler());
  }

  @Test
  public void testProperties() {
    assertEquals("scott_reynolds.jpg", asset.getName());
    assertEquals("/content/dam/sample/portraits/scott_reynolds.jpg", asset.getPath());
    assertNotEquals(0, asset.hashCode());

    if (context.resourceResolverType() == ResourceResolverType.JCR_OAK) {
      assertNotNull(asset.getIdentifier());
    }
    else {
      assertEquals("442d55b6-d534-4faf-9394-c9c20d095985", asset.getIdentifier());
    }
  }

  @Test
  public void testRenditions() {
    List<Rendition> renditions = IteratorUtils.toList(asset.listRenditions());
    assertEquals(4, renditions.size());
    assertTrue(hasRendition(renditions, "cq5dam.thumbnail.48.48.png"));
    assertEquals("original", asset.getRendition("original").getName());
  }

  private boolean hasRendition(List<Rendition> renditions, String renditionName) {
    for (Rendition rendition : renditions) {
      if (StringUtils.equals(rendition.getName(), renditionName)) {
        return true;
      }
    }
    return false;
  }

  @Test
  @SuppressWarnings("java:S2699") // assert not exception is thrown
  public void testRemoveNonExistingRendition() {
    asset.removeRendition("non-existing");
  }

  @Test
  @SuppressWarnings("null")
  public void testEquals() {
    Asset asset1 = this.context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg").adaptTo(Asset.class);
    Asset asset2 = this.context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg").adaptTo(Asset.class);

    assertEquals(asset1, asset2);
  }

  private void doTestAddRemoveRendition(final String renditionName) {
    InputStream is = new ByteArrayInputStream(BINARY_DATA);
    Rendition rendition = asset.setRendition(renditionName, is, Map.of("jcr:mimeType", "application/octet-stream"));

    assertNotNull(rendition);
    assertNotNull(asset.getRendition(renditionName));
    Resource resource = context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/" + renditionName);
    assertNotNull(resource);

    Optional<DamEvent> damEvent = damEventHandler.getLastEvent();
    assertTrue(damEvent.isPresent());
    assertEquals(DamEvent.Type.RENDITION_UPDATED, damEvent.get().getType());
    assertEquals(asset.getPath(), damEvent.get().getAssetPath());
    assertEquals(rendition.getPath(), damEvent.get().getAdditionalInfo());

    asset.removeRendition(renditionName);

    assertNull(asset.getRendition(renditionName));
    resource = context.resourceResolver().getResource("/content/dam/sample/portraits/scott_reynolds.jpg/jcr:content/renditions/" + renditionName);
    assertNull(resource);

    damEvent = damEventHandler.getLastEvent();
    assertTrue(damEvent.isPresent());
    assertEquals(DamEvent.Type.RENDITION_REMOVED, damEvent.get().getType());
    assertEquals(asset.getPath(), damEvent.get().getAssetPath());
    assertEquals(rendition.getPath(), damEvent.get().getAdditionalInfo());
  }

  @Test
  public void testAddRemoveRendition() {
    doTestAddRemoveRendition("test.bin");
  }

  @Test
  public void testCQAdaptation() {
    com.day.cq.dam.api.Asset cqAsset = asset.adaptTo(com.day.cq.dam.api.Asset.class);
    assertNotNull(cqAsset);
    assertEquals(asset, cqAsset.adaptTo(Asset.class));
  }

}
