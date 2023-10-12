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

import org.apache.sling.api.adapter.AdapterFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.annotations.Component;

import com.adobe.cq.xf.ExperienceFragment;
import com.adobe.cq.xf.ExperienceFragmentVariation;
import com.adobe.cq.xf.ExperienceFragmentsConstants;
import com.day.cq.wcm.api.Page;

/**
 * Mock adapter factory for AEM Experience Fragment-related adaptions.
 */
@Component(service = AdapterFactory.class,
    property = {
        AdapterFactory.ADAPTABLE_CLASSES + "=com.day.cq.wcm.api.Page",
        AdapterFactory.ADAPTER_CLASSES + "=com.adobe.cq.xf.ExperienceFragment",
        AdapterFactory.ADAPTER_CLASSES + "=com.adobe.cq.xf.ExperienceFragmentVariation"
    })
@ProviderType
public class MockExperienceFragmentAdapterFactory implements AdapterFactory {

  @SuppressWarnings("unchecked")
  @Override
  public @Nullable <AdapterType> AdapterType getAdapter(@NotNull Object object, @NotNull Class<AdapterType> type) {
    if (object instanceof Page) {
      Page page = (Page)object;
      if (page.getContentResource().isResourceType(ExperienceFragmentsConstants.RT_EXPERIENCE_FRAGMENT_MASTER)) {
        if (type == ExperienceFragment.class) {
          return (AdapterType)new MockExperienceFragment(page);
        }
      }
      if (page.getContentResource().isResourceType(ExperienceFragmentsConstants.RT_EXPERIENCE_FRAGMENT_PAGE)) {
        if (type == ExperienceFragmentVariation.class) {
          return (AdapterType)new MockExperienceFragmentVariation(page);
        }
      }
    }
    return null;
  }

}
