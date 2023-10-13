/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.testing.mock.aem.xf;

import static com.adobe.cq.xf.ExperienceFragmentsConstants.TYPE_XF_VARIANT_FACEBOOK;
import static com.adobe.cq.xf.ExperienceFragmentsConstants.TYPE_XF_VARIANT_WEB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.adobe.cq.xf.ExperienceFragment;
import com.adobe.cq.xf.ExperienceFragmentVariation;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockExperienceFragmentTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  @Before
  public void setUp() {
    ContentLoader contentLoader = this.context.load();
    contentLoader.json("/json-import-samples/xf.json", "/content/experience-fragments/sample");
  }

  @Test
  @SuppressWarnings("null")
  public void testExperienceFragment() {
    Resource xfResource = this.context.resourceResolver().getResource("/content/experience-fragments/sample");
    Page xfPage = xfResource.adaptTo(Page.class);
    assertNull(xfPage.adaptTo(ExperienceFragmentVariation.class));
    ExperienceFragment experienceFragment = xfPage.adaptTo(ExperienceFragment.class);
    assertNotNull(experienceFragment);

    Resource adaptedResource = experienceFragment.adaptTo(Resource.class);
    assertEquals(xfResource, adaptedResource);
    Page adaptedPage = experienceFragment.adaptTo(Page.class);
    assertEquals(xfPage, adaptedPage);

    assertEquals("/content/experience-fragments/sample", experienceFragment.getPath());
    assertEquals("/conf/something", experienceFragment.getCloudserviceConfigurationsPaths().get(0));
    assertEquals(2, experienceFragment.getVariations().size());
    assertEquals(1, experienceFragment.getVariations(TYPE_XF_VARIANT_FACEBOOK).size());
    assertEquals("/content/experience-fragments/sample/master", experienceFragment.getVariations().get(0).getPath());
    assertEquals("Header", experienceFragment.getProperties().get("jcr:title"));
  }

  @Test
  @SuppressWarnings("null")
  public void testExperienceFragmentVariation() {
    Resource masterResource = this.context.resourceResolver().getResource("/content/experience-fragments/sample/master");
    Page masterPage = masterResource.adaptTo(Page.class);
    assertNull(masterPage.adaptTo(ExperienceFragment.class));
    ExperienceFragmentVariation variation = masterPage.adaptTo(ExperienceFragmentVariation.class);
    assertNotNull(variation);

    Resource adaptedResource = variation.adaptTo(Resource.class);
    assertEquals(masterResource, adaptedResource);
    Page adaptedPage = variation.adaptTo(Page.class);
    assertEquals(masterPage, adaptedPage);

    assertEquals("/content/experience-fragments/sample/master", variation.getPath());
    assertEquals("/conf/something", variation.getCloudserviceConfigurationsPaths().get(0));
    assertEquals("/conf/something", variation.getPropertiesTree().getInherited("cq:cloudserviceconfigs", ""));
    assertEquals(TYPE_XF_VARIANT_WEB, variation.getType());
    assertEquals("/content/experience-fragments/sample", variation.getParent().getPath());
    assertEquals("Header", variation.getProperties().get("jcr:title"));
  }

}
