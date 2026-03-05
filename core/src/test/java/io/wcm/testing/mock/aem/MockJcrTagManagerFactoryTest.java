/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2026 wcm.io
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

import static org.junit.Assert.assertNotNull;

import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.tagging.JcrTagManagerFactory;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockJcrTagManagerFactoryTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private JcrTagManagerFactory underTest;

  @Before
  public void setUp() {
    context.create().tag("test:test-tag");
    underTest = context.getService(JcrTagManagerFactory.class);
  }

  @Test
  public void testGetTagManagerFromResourceResolver() {
    TagManager tagManager = underTest.getTagManager(context.resourceResolver());
    assertNotNull(tagManager);

    Tag tag = tagManager.resolve("test:test-tag");
    assertNotNull(tag);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetTagManagerFromResourceResolverWithNull() {
    underTest.getTagManager((ResourceResolver)null);
  }

  @SuppressWarnings("deprecation")
  @Test(expected = UnsupportedOperationException.class)
  public void testGetTagManagerFromSession() {
    underTest.getTagManager((Session)null);
  }

}
