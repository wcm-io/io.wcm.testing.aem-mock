/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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
package io.wcm.testing.mock.aem;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.apache.sling.api.resource.Resource;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;
import com.day.image.Layer;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

/**
 * Test loading JCR content from FileVault XML and JSON folder structures
 * and access the data via various AEM APIs.
 */
public class ContentLoaderFolderTest {

  private static final File FOLDER_CONTENT_SAMPLE = new File("src/test/resources/folder-content-sample");
  private static final String STRUCTURE_ELEMENT_TEMPLATE_PATH = "/apps/myproject1/templates/structureElement";
  private static final String SAMPLE_COMPONENT_PATH = "/apps/myproject1/components/customcarousel";

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  @Test
  @SuppressWarnings("null")
  public void testAppsJSON() {
    context.load().folderJson(new File(FOLDER_CONTENT_SAMPLE, "apps-json"), "/apps/myproject1");

    Resource templateResource = context.resourceResolver().getResource(STRUCTURE_ELEMENT_TEMPLATE_PATH);
    assertNotNull(templateResource);
    assertEquals("myproject1 Structure Element", templateResource.getValueMap().get(JCR_TITLE));

    Template template = templateResource.adaptTo(Template.class);
    assertEquals("myproject1 Structure Element", template.getTitle());

    ComponentManager componentManager = context.resourceResolver().adaptTo(ComponentManager.class);
    Component component = componentManager.getComponent(SAMPLE_COMPONENT_PATH);
    assertNotNull(component);
    assertEquals("Carousel (Custom)", component.getTitle());
  }

  @Test
  @SuppressWarnings("null")
  public void testAppsFileVault() {
    context.load().folderFileVaultXml(new File(FOLDER_CONTENT_SAMPLE, "apps-filevault"), "/apps/myproject1");

    Resource templateResource = context.resourceResolver().getResource(STRUCTURE_ELEMENT_TEMPLATE_PATH);
    assertNotNull(templateResource);
    assertEquals("myproject1 Structure Element", templateResource.getValueMap().get(JCR_TITLE));

    Template template = templateResource.adaptTo(Template.class);
    assertEquals("myproject1 Structure Element", template.getTitle());

    ComponentManager componentManager = context.resourceResolver().adaptTo(ComponentManager.class);
    Component component = componentManager.getComponent(SAMPLE_COMPONENT_PATH);
    assertNotNull(component);
    assertEquals("Carousel (Custom)", component.getTitle());
  }

  @Test
  public void testContentConfFileVault() {
    context.load().folderFileVaultXml(new File(FOLDER_CONTENT_SAMPLE, "conf-filevault"), "/conf/myproject1");
    context.load().folderFileVaultXml(new File(FOLDER_CONTENT_SAMPLE, "content-filevault"), "/content/myproject1");

    Page testPage = context.pageManager().getPage("/content/myproject1/test");
    assertNotNull(testPage);
    assertEquals("Test", testPage.getTitle());

    Template template = testPage.getTemplate();
    assertNotNull(template);
    assertEquals("myproject1 Content", template.getTitle());
  }

  @Test
  @SuppressWarnings("null")
  public void testAsset() {
    context.load().folderFileVaultXml(new File(FOLDER_CONTENT_SAMPLE, "content-filevault"), "/content/dam/myproject1");

    Resource assetResource = context.resourceResolver().getResource("/content/dam/myproject1/sample.jpg");
    assertNotNull(assetResource);
    Asset asset = assetResource.adaptTo(Asset.class);
    assertEquals("sample.jpg", asset.getName());

    Layer layer = asset.getOriginal().adaptTo(Layer.class);
    assertNotNull(layer);
    assertEquals(100, layer.getWidth());
    assertEquals(50, layer.getHeight());
  }

}
