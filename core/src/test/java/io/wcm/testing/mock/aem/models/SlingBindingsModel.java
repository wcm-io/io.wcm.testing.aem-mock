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
package io.wcm.testing.mock.aem.models;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.xss.XSSAPI;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.api.policies.ContentPolicy;

@Model(adaptables = SlingHttpServletRequest.class)
public interface SlingBindingsModel {

  // -- Sling --
  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  ResourceResolver getResolver();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  Resource getResource();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  SlingHttpServletRequest getRequest();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  SlingHttpServletResponse getResponse();

  // -- JCR --
  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  Node getCurrentNode();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  Session getcurrentSession();

  // -- AEM --
  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  ComponentContext getComponentContext();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  EditContext getEditContext();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  ValueMap getProperties();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  PageManager getPageManager();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  Page getCurrentPage();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  Page getResourcePage();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  ValueMap getPageProperties();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  Component getComponent();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  Designer getDesigner();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  Design getCurrentDesign();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  Design getResourceDesign();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  Style getCurrentStyle();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  XSSAPI getXssAPI();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  ContentPolicy getCurrentContentPolicy();

  @ScriptVariable(injectionStrategy = InjectionStrategy.OPTIONAL)
  ValueMap getCurrentContentPolicyProperties();

}
