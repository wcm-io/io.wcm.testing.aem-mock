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

import java.util.Arrays;
import java.util.List;

import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.api.Page;

class MockExperienceFragmentBase extends SlingAdaptable {

  private final Page page;

  protected MockExperienceFragmentBase(Page page) {
    this.page = page;
  }

  public String getPath() {
    return page.getPath();
  }

  public ValueMap getProperties() {
    return page.getProperties();
  }

  public List<String> getCloudserviceConfigurationsPaths() {
    return Arrays.asList(getInheritedProperties().getInherited("cq:cloudserviceconfigs", new String[0]));
  }

  protected InheritanceValueMap getInheritedProperties() {
    return new HierarchyNodeInheritanceValueMap(page.getContentResource());
  }

  @Override
  @SuppressWarnings({ "unchecked", "null" })
  public @Nullable <AdapterType> AdapterType adaptTo(@NotNull Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)page.adaptTo(Resource.class);
    }
    if (type == Page.class) {
      return (AdapterType)page;
    }
    return super.adaptTo(type);
  }

  protected Page getPage() {
    return page;
  }

}
