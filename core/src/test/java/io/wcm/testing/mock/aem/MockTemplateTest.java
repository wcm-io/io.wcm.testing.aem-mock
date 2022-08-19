/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Template;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

@SuppressWarnings("null")
public class MockTemplateTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private Template template;

  @Before
  public void setUp() throws Exception {
    context.load().json("/json-import-samples/application.json", "/apps/sample");

    Resource resource = this.context.resourceResolver().getResource("/apps/sample/templates/homepage");
    this.template = resource.adaptTo(Template.class);
  }

  @Test
  public void testProperties() {
    assertEquals("/apps/sample/templates/homepage", this.template.getPath());
    assertEquals("homepage", this.template.getName());
    assertEquals("Sample Homepage", this.template.getTitle());
    assertEquals("Homepage", this.template.getShortTitle());
    assertNotNull(this.template.getDescription());
    assertNull(this.template.getIconPath());
    assertEquals("/apps/sample/templates/homepage/thumbnail.png", this.template.getThumbnailPath());
    assertEquals((Long)110L, this.template.getRanking());
    assertEquals((Integer)110, this.template.getProperties().get("ranking", 0));
    assertNotEquals(template.hashCode(), 0);
  }

  @Test
  public void testEquals() throws Exception {
    Template template1 = this.context.resourceResolver().getResource("/apps/sample/templates/homepage").adaptTo(Template.class);
    Template template2 = this.context.resourceResolver().getResource("/apps/sample/templates/homepage").adaptTo(Template.class);
    Template template3 = this.context.resourceResolver().getResource("/apps/sample/templates/contentpage").adaptTo(Template.class);

    assertEquals(template1, template2);
    assertNotEquals(template1, template3);
  }

  @Test
  public void testEditableTemplateProperties() {
    if (context.resourceResolverType() == ResourceResolverType.RESOURCERESOLVER_MOCK) {
      // skip - FsResourceProvider not supported
      return;
    }

    context.load().folderFileVaultXml("src/test/resources/folder-content-sample/conf-filevault", "/conf/myproject1");
    Resource resource = context.resourceResolver().getResource("/conf/myproject1/settings/wcm/templates/contentpage");
    this.template = resource.adaptTo(Template.class);

    assertEquals("/conf/myproject1/settings/wcm/templates/contentpage", this.template.getPath());
    assertEquals("contentpage", this.template.getName());
    assertEquals("myproject1 Content", this.template.getTitle());
    assertEquals((Long)10L, this.template.getRanking());
    assertEquals((Integer)10, this.template.getProperties().get("ranking", 0));
  }

}
