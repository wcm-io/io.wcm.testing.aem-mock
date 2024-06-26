/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.AnalyzeContext;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.ComponentManager;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.api.designer.Cell;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Mock implementation of {@link ComponentContext}.
 */
public final class MockComponentContext implements ComponentContext {

  private final Page currentPage;
  private final SlingHttpServletRequest request;
  private final EditContext editContext;

  private final Map<String, Object> attributes = new HashMap<>();
  private boolean decorate = true;
  private String decorationTagName;
  private String defaultDecorationTagName;
  private Cell cell;
  private Set<String> cssClassNames = Collections.emptySet();

  /**
   * @param currentPage Current page
   * @param request Request
   */
  public MockComponentContext(@NotNull Page currentPage, @NotNull SlingHttpServletRequest request) {
    this.currentPage = currentPage;
    this.request = request;
    boolean hasEditContext = WCMMode.fromRequest(request) != WCMMode.DISABLED;
    editContext = hasEditContext ? new MockEditContext(this) : null;
    cell = new MockCell(this);
  }

  @Override
  public Page getPage() {
    return currentPage;
  }

  @Override
  public Resource getResource() {
    return request.getResource();
  }

  @Override
  @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
  @SuppressWarnings("java:S2583") // null check
  public Component getComponent() {
    Resource currentResource = getResource();
    if (currentResource == null) {
      return null;
    }
    ComponentManager componentManager = currentResource.getResourceResolver().adaptTo(ComponentManager.class);
    if (componentManager == null) {
      throw new IllegalStateException("No component manager.");
    }
    return componentManager.getComponentOfResource(currentResource);
  }

  @Override
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public Object setAttribute(String name, Object value) {
    return attributes.put(name, value);
  }

  @Override
  public boolean hasDecoration() {
    return decorate;
  }

  @Override
  public void setDecorate(boolean decorate) {
    this.decorate = decorate;
  }

  @Override
  public String getDecorationTagName() {
    return decorationTagName;
  }

  @Override
  public void setDecorationTagName(String value) {
    this.decorationTagName = value;
  }

  @Override
  public String getDefaultDecorationTagName() {
    return defaultDecorationTagName;
  }

  @Override
  public void setDefaultDecorationTagName(String value) {
    this.defaultDecorationTagName = value;
  }

  @Override
  public EditContext getEditContext() {
    return editContext;
  }

  @Override
  public Cell getCell() {
    return cell;
  }

  /**
   * @param cell Cell
   */
  public void setCell(Cell cell) {
    this.cell = cell;
  }

  @Override
  public Set<String> getCssClassNames() {
    return cssClassNames;
  }

  /**
   * @param cssClassNames CSS class names
   */
  public void setCssClassNames(Set<String> cssClassNames) {
    this.cssClassNames = cssClassNames;
  }

  @Override
  public ComponentContext getParent() {
    return null;
  }

  @Override
  public ComponentContext getRoot() {
    return this;
  }

  @Override
  public boolean isRoot() {
    return true;
  }


  // --- unsupported operations ---

  @Override
  public AnalyzeContext getAnalyzeContext() {
    throw new UnsupportedOperationException();
  }

}
