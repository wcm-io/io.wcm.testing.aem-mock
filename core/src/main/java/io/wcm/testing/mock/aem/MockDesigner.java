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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.commons.WCMUtils;
import com.day.text.Text;

/**
 * Mock implementation of {@link Designer}.
 */
class MockDesigner implements Designer {

  private static final Logger log = LoggerFactory.getLogger(MockDesigner.class);

  @SuppressWarnings({
      "java:S1075", "deprecation"
  }) // Repository path
  static final String LEGACY_DEFAULT_DESIGN_PATH = DEFAULT_DESIGN_PATH;
  static final String LEGACY_DESIGNS_PATH_PREFIX = StringUtils.substringBeforeLast(LEGACY_DEFAULT_DESIGN_PATH, String.valueOf('/')) + '/';

  @SuppressWarnings("java:S1075") // Repository path
  static final String LIBS_DEFAULT_DESIGN_PATH = "/libs/settings/wcm/designs/default";

  static final Set<String> DEFAULT_DESIGN_PATHS = new LinkedHashSet<>();

  static {
    DEFAULT_DESIGN_PATHS.add(LEGACY_DEFAULT_DESIGN_PATH);
    DEFAULT_DESIGN_PATHS.add(LIBS_DEFAULT_DESIGN_PATH);
  }

  private final ResourceResolver resourceResolver;

  MockDesigner(ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  @Override
  public String getDesignPath(Page page) {
    if (page == null) {
      return null;
    }
    final String path = WCMUtils.getInheritedProperty(page, resourceResolver, NameConstants.PN_DESIGN_PATH);
    if (path != null) {
      return path;
    }
    if (resourceResolver.getResource(LEGACY_DEFAULT_DESIGN_PATH) != null) {
      return LEGACY_DEFAULT_DESIGN_PATH;
    }
    return LIBS_DEFAULT_DESIGN_PATH;
  }

  @Override
  public Design getDesign(Page page) {
    if (page == null) {
      return null;
    }
    final String designPath = getDesignPath(page);
    if (designPath == null) {
      return getDefaultDesign();
    }
    return getDesign(designPath);
  }

  @Override
  public boolean hasDesign(String id) {
    final Design design = getDesign(idToPath(id));
    return !DEFAULT_DESIGN_PATHS.contains(design.getPath());
  }

  @Override
  public Design getDesign(String id) {
    final String path = idToPath(id);
    final Resource resource = resourceResolver.getResource(path);
    if (resource != null) {
      return new MockDesign(resource);
    }
    if (!DEFAULT_DESIGN_PATHS.contains(path)) {
      log.warn("Design with path {} not found, returning the default design", path);
    }
    return getDefaultDesign();
  }

  @Override
  public Style getStyle(Resource resource) {
    return getStyle(resource, null);
  }

  @Override
  public Style getStyle(Resource resource, String cellPath) {
    final PageManager pageManager = Objects.requireNonNull(resourceResolver.adaptTo(PageManager.class));
    final Page page = pageManager.getContainingPage(resource);
    if (page != null) {
      final Design design = this.getDesign(page);
      if (design != null) {
        return design.getStyle(cellPath == null ? Text.getName(resource.getPath()) : cellPath);
      }
    }
    return null;
  }

  @Override
  public Design getDefaultDesign() {
    for (final String path : DEFAULT_DESIGN_PATHS) {
      final Resource designResource = resourceResolver.getResource(path);
      if (designResource != null) {
        return new MockDesign(designResource);
      }
    }
    return new MockDesign(new NonExistingResource(resourceResolver, LIBS_DEFAULT_DESIGN_PATH));
  }

  @NotNull
  private String idToPath(@NotNull final String id) {
    if (Strings.CS.startsWithAny(id, ArrayUtils.add(resourceResolver.getSearchPath(), "/conf/"))) {
      return id;
    }
    return LEGACY_DESIGNS_PATH_PREFIX + Strings.CS.removeStart(Strings.CS.removeStart(id, LEGACY_DESIGNS_PATH_PREFIX), String.valueOf('/'));
  }
}
