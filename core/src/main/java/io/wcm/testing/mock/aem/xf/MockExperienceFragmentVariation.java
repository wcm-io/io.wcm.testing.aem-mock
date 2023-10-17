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

import static com.adobe.cq.xf.ExperienceFragmentsConstants.CUSTOM_XF_VARIANT_TYPE;
import static com.adobe.cq.xf.ExperienceFragmentsConstants.PN_XF_VARIANT_TYPE;

import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.adobe.cq.xf.ExperienceFragment;
import com.adobe.cq.xf.ExperienceFragmentVariation;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.api.Page;

/**
 * Mock implementation of {@link ExperienceFragmentVariation}.
 */
class MockExperienceFragmentVariation extends MockExperienceFragmentBase implements ExperienceFragmentVariation {

  private ExperienceFragment parent;

  MockExperienceFragmentVariation(Page page) {
    super(page);
  }

  @Override
  public ExperienceFragment getParent() {
    if (this.parent == null) {
      this.parent = new MockExperienceFragment(getPage().getParent());
    }
    return this.parent;
  }

  @Override
  public String getType() {
    ValueMap properties = getPage().getProperties();
    String type = properties.get(PN_XF_VARIANT_TYPE, String.class);
    return (type != null) ? type : CUSTOM_XF_VARIANT_TYPE;
  }

  @Override
  @SuppressWarnings("unchecked")
  public @Nullable <AdapterType> AdapterType adaptTo(@NotNull Class<AdapterType> type) {
    if (type == Page.class) {
      return (AdapterType)getPage();
    }
    return super.adaptTo(type);
  }

  @Override
  public InheritanceValueMap getPropertiesTree() {
    return getInheritedProperties();
  }

}
