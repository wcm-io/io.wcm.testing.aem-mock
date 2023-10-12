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

import static com.adobe.cq.xf.ExperienceFragmentsConstants.PN_XF_VARIANT_TYPE;
import static com.adobe.cq.xf.ExperienceFragmentsConstants.RT_EXPERIENCE_FRAGMENT_PAGE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;

import com.adobe.cq.xf.ExperienceFragment;
import com.adobe.cq.xf.ExperienceFragmentVariation;
import com.day.cq.commons.Filter;
import com.day.cq.wcm.api.Page;

/**
 * Mock implementation of {@link ExperienceFragment}.
 */
class MockExperienceFragment extends MockExperienceFragmentBase implements ExperienceFragment {

  MockExperienceFragment(Page page) {
    super(page);
  }

  @Override
  public List<ExperienceFragmentVariation> getVariations() {
    List<ExperienceFragmentVariation> variations = new ArrayList<>();
    Iterator<Page> it = getPage().listChildren(element -> element.getContentResource().isResourceType(RT_EXPERIENCE_FRAGMENT_PAGE));
    while (it.hasNext()) {
      Page thePage = it.next();
      variations.add(thePage.adaptTo(ExperienceFragmentVariation.class));
    }
    return variations;
  }

  @Override
  public List<ExperienceFragmentVariation> getVariations(String... type) {
    final Set<String> typeValues = new HashSet<>(List.of(type));
    Filter<Page> typeFilter = element -> {
      ValueMap properties = element.getProperties();
      String variantType = properties.get(PN_XF_VARIANT_TYPE, "");
      if (StringUtils.isEmpty(variantType)) {
        return false;
      }
      return typeValues.contains(variantType);
    };
    List<ExperienceFragmentVariation> variations = new ArrayList<>();
    for (Iterator<Page> it = getPage().listChildren(typeFilter); it.hasNext();) {
      Page page = it.next();
      variations.add(page.adaptTo(ExperienceFragmentVariation.class));
    }
    return variations;
  }

}
