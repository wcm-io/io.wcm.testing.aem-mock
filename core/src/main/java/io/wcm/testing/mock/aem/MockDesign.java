/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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

import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.jetbrains.annotations.NotNull;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.designer.Cell;
import com.day.cq.wcm.api.designer.ComponentStyle;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Mock implementation of {@link Design}.
 */
class MockDesign implements Design {

  private static final Set<String> JSON_EXCLUDE_PROPERTY_NAMES = Set.of(
          JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
          JcrConstants.JCR_PRIMARYTYPE,
          JcrConstants.JCR_MIXINTYPES
  );

  private static final DateTimeFormatter JSON_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.US);

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  private final Style emptyStyle = new MockStyle(ValueMap.EMPTY, this);
  private final Resource resource;

  MockDesign(@NotNull Resource resource) {
    this.resource = resource;
  }

  @Override
  public Style getStyle(String path) {
    Resource styleResource = this.resource.getResourceResolver().getResource(path);
    if (styleResource != null) {
      return getStyle(styleResource);
    }
    return emptyStyle;
  }

  @Override
  public Style getStyle(Cell cell) {
    if (cell instanceof MockCell) {
      Resource styleResource = ((MockCell)cell).getComponentContext().getResource();
      return getStyle(styleResource);
    }
    return emptyStyle;
  }

  @Override
  public Style getStyle(Resource res) {
    ContentPolicyManager contentPolicyManager = res.getResourceResolver().adaptTo(ContentPolicyManager.class);
    if (contentPolicyManager instanceof MockContentPolicyManager) {
      // unwrap resource to make sure the correct resource type is used when using resource-type forcing wrappers
      Resource unwrappedResource = ResourceUtil.unwrap(res);
      ContentPolicy policy = ((MockContentPolicyManager)contentPolicyManager).getPolicy(unwrappedResource);
      if (policy != null) {
        return new MockStyle(policy.getProperties(), this);
      }
    }
    return emptyStyle;
  }

  @Override
  public Style getStyle(Resource res, boolean ignoreExcludedComponents) {
    return getStyle(res);
  }

  @Override
  public String getPath() {
    return resource.getPath();
  }

  @Override
  public Resource getContentResource() {
    return this.resource.getChild(JcrConstants.JCR_CONTENT);
  }

  @Override
  public String getId() {
    return StringUtils.removeStart(resource.getPath(), MockDesigner.LEGACY_DESIGNS_PATH_PREFIX);
  }

  @Override
  public String getJSON() {
    final Resource contentResource = this.getContentResource();
    if (contentResource != null) {
      try {
        final Map<String, Object> map = new HashMap<>();
        addSafePropertiesToJson(map, contentResource);
        return JSON_MAPPER.writeValueAsString(map);
      }
      catch (JsonProcessingException ex) {
        throw new RuntimeException("Unable to serialize design content resource properties to JSON", ex);
      }
    }
    return "{}";
  }


  // --- unsupported operations ---

  @Override
  public Map<String, ComponentStyle> getComponentStyles(Cell cell) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getCssPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("deprecation")
  public com.day.cq.commons.Doctype getDoctype(Style style) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Calendar getLastModified() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getStaticCssPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasContent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeCSS(Writer writer, boolean includeCustom) throws IOException, RepositoryException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeCssIncludes(Writer writer) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeCssIncludes(PageContext pageContext) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("deprecation")
  public void writeCssIncludes(Writer writer, com.day.cq.commons.Doctype doctype) throws IOException {
    throw new UnsupportedOperationException();
  }

  private void addSafePropertiesToJson(@NotNull final Map<String, Object> map,
                                       @NotNull final Resource contentResource) {
    contentResource.getValueMap().entrySet().stream()
        .filter(entry -> !JSON_EXCLUDE_PROPERTY_NAMES.contains(entry.getKey()))
        .forEach(entry -> {
          if (entry.getValue() instanceof Calendar) {
            Calendar calendar = (Calendar) entry.getValue();
            map.put(entry.getKey(), JSON_DATE_FORMAT.format(calendar.toInstant().atZone(calendar.getTimeZone().toZoneId())));
          } else {
            map.put(entry.getKey(), entry.getValue());
          }
        });
    contentResource.getChildren().forEach(child -> {
      final Map<String, Object> subMap = new HashMap<>();
      addSafePropertiesToJson(subMap, child);
      map.put(child.getName(), subMap);
    });
  }
}
