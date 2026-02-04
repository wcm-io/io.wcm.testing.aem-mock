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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.jsp.PageContext;

import com.day.cq.commons.jcr.JcrConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.designer.Cell;
import com.day.cq.wcm.api.designer.ComponentStyle;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.jetbrains.annotations.NotNull;

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
  public Style getStyle(Resource resource) {
    ContentPolicyManager contentPolicyManager = resource.getResourceResolver().adaptTo(ContentPolicyManager.class);
    if (contentPolicyManager instanceof MockContentPolicyManager) {
      // unwrap resource to make sure the correct resource type is used when using resource-type forcing wrappers
      Resource unwrappedResource = ResourceUtil.unwrap(resource);
      ContentPolicy policy = ((MockContentPolicyManager)contentPolicyManager).getPolicy(unwrappedResource);
      if (policy != null) {
        return new MockStyle(policy.getProperties(), this);
      }
    }
    return emptyStyle;
  }

  @Override
  public Style getStyle(Resource resource, boolean ignoreExcludedComponents) {
    return getStyle(resource);
  }

  @Override
  public String getPath() {
    return resource.getPath();
  }


  // --- unsupported operations ---

  @Override
  public Map<String, ComponentStyle> getComponentStyles(Cell cell) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Resource getContentResource() {
    return this.resource.getChild(JcrConstants.JCR_CONTENT);
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
  public String getId() {
    return StringUtils.removeStart(resource.getPath(), MockDesigner.LEGACY_DESIGNS_PATH_PREFIX);
  }

  @Override
  public String getJSON() {
    final JsonObjectBuilder builder = Json.createObjectBuilder();
    final Resource contentResource = this.getContentResource();
    if (contentResource != null) {
      addSafePropertiesToJson(builder, contentResource);
    }
    return builder.build().toString();
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

  private static void addSafePropertiesToJson(@NotNull final JsonObjectBuilder builder, @NotNull final Resource resource) {
    resource.getValueMap().forEach((key, value) -> {
      if (JSON_EXCLUDE_PROPERTY_NAMES.contains(key)) {
        return;
      }
      if (value instanceof String) {
        builder.add(key, (String) value);
      } else if (value instanceof Long) {
        builder.add(key, (long) value);
      } else if (value instanceof Integer) {
        builder.add(key, (int) value);
      } else if (value instanceof Boolean) {
        builder.add(key, (boolean) value);
      } else if (value instanceof Calendar) {
        final Calendar calendar = (Calendar) value;
        builder.add(key, JSON_DATE_FORMAT.format(calendar.toInstant().atZone(calendar.getTimeZone().toZoneId())));
      } else {
        throw new RuntimeException("Unrecognized property value of type " + value.getClass());
      }
    });

    resource.getChildren().forEach(child -> {
      final JsonObjectBuilder subObject = Json.createObjectBuilder();
      addSafePropertiesToJson(subObject, child);
      builder.add(child.getName(), subObject);
    });
  }
}
