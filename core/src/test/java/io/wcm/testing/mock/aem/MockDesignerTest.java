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

import static io.wcm.testing.mock.aem.MockDesigner.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.designer.Design;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.context.TestAemContext;
import io.wcm.testing.mock.aem.junit.AemContext;

public class MockDesignerTest {

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  @Test
  public void testGetDesignPathForNullPage() {
    assertNull(context.designer().getDesignPath(null));
  }

  @Test
  public void testGetDesignPathForNonExistingDesign() {
    final Page page = context.create().page("/content/page1");
    assertEquals(LIBS_DEFAULT_DESIGN_PATH, context.designer().getDesignPath(page));
  }

  @Test
  public void testGetDesignPathForNonExistingDesignLegacy() {
    context.create().design(LEGACY_DEFAULT_DESIGN_PATH);
    final Page page = context.create().page("/content/page1");
    assertEquals(LEGACY_DEFAULT_DESIGN_PATH, context.designer().getDesignPath(page));
  }

  @Test
  public void testGetDesignPageNullPage() {
    assertNull(context.designer().getDesign((Page)null));
  }

  @Test
  public void testGetDesignPageWithoutDesign() {
    final Page page = context.create().page("/content/page1");
    assertThat(context.designer().getDesign(page), is(designWithPath(LIBS_DEFAULT_DESIGN_PATH)));
  }

  @Test
  public void testGetDesignPage() {
    final Design design = context.create().design("/etc/designs/test");
    final Page page = context.create().page("/content/page1", null,
            NameConstants.PN_DESIGN_PATH, design.getPath());
    assertThat(context.designer().getDesign(page), is(designWithPath(design.getPath())));
  }

  @Test
  public void testHasDesign() {
    assertFalse(context.designer().hasDesign("/any/id"));
  }

  @Test
  public void testGetDesignString() {
    assertThat(context.designer().getDesign("/any/id"), is(designWithPath(LIBS_DEFAULT_DESIGN_PATH)));
  }

  @Test
  public void testGetStyleResource() {
    final Page page = context.create().page("/content/page1");
    assertNotNull(context.designer().getStyle(page.getContentResource()));
  }

  @Test
  public void testGetStyleResourceString() {
    final Page page = context.create().page("/content/page1");
    assertNotNull(context.designer().getStyle(page.getContentResource(), "anyCell"));
  }

  @Test
  public void testGetDefaultDesignNonExisting() {
      assertThat(context.designer().getDefaultDesign(), designWithPath(LIBS_DEFAULT_DESIGN_PATH));
  }

  @Test
  public void testGetDefaultDesign() {
    context.create().design(LIBS_DEFAULT_DESIGN_PATH);
    assertThat(context.designer().getDefaultDesign(), designWithPath(LIBS_DEFAULT_DESIGN_PATH));
  }

  @Test
  public void testGetDefaultDesignLegacy() {
    context.create().design(LEGACY_DEFAULT_DESIGN_PATH);
    assertThat(context.designer().getDefaultDesign(), designWithPath(LEGACY_DEFAULT_DESIGN_PATH));
  }

  @NotNull
  private static Matcher<Design> designWithPath(@NotNull final String expectedPath) {
    return new TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(@NotNull final Design design) {
        return expectedPath.equals(design.getPath());
      }

      @Override
      public void describeTo(Description description) {
        description
          .appendText("Design with path ")
          .appendValue(expectedPath);
      }

      @Override
      protected void describeMismatchSafely(Design item, Description mismatchDescription) {
        mismatchDescription
          .appendText("Design with path ")
          .appendValue(item.getPath());
      }
    };
  }
}
