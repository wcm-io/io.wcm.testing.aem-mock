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
package io.wcm.testing.mock.aem.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import io.wcm.testing.mock.aem.MockOverrideSlingPathRequestWrapper;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.models.SlingBindingsModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.scripting.api.BindingsValuesProvidersByContext;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@SuppressWarnings("null")
public class MockAemSlingBindingsTest {

  private static final String COMPONENT_RESOURCE_SUPER_TYPE = "app1/components/component2";
  private static final String COMPONENT_RESOURCE_TYPE = "app1/components/component1";
  private static final String CHILD_COMPOSITE_COMPONENT_RESOURCE_TYPE = COMPONENT_RESOURCE_TYPE + "/child1";

  @Rule
  public AemContext context = TestAemContext.newAemContext();

  private Page currentPage;
  private Resource currentResource;

  @Before
  public void setUp() throws Exception {
    context.addModelsForClasses(SlingBindingsModel.class);

    currentPage = context.create().page("/content/testPage");
    currentResource = context.create().resource(currentPage.getContentResource().getPath() + "/testResource",
        "sling:resourceType", COMPONENT_RESOURCE_TYPE);

    context.create().resource("/apps/" + COMPONENT_RESOURCE_SUPER_TYPE,
        JcrConstants.JCR_PRIMARYTYPE, NameConstants.NT_COMPONENT);

    context.create().resource("/apps/" + COMPONENT_RESOURCE_TYPE,
        JcrConstants.JCR_PRIMARYTYPE, NameConstants.NT_COMPONENT,
        "sling:resourceSuperType", COMPONENT_RESOURCE_SUPER_TYPE);
  }

  @Test
  public void testBindings() {
    context.currentResource(currentResource);

    SlingBindingsModel model = context.request().adaptTo(SlingBindingsModel.class);

    assertNotNull(model);

    assertNotNull(model.getResolver());
    assertNotNull(model.getResource());
    assertNotNull(model.getRequest());
    assertNotNull(model.getResponse());
    if (context.resourceResolverType() == ResourceResolverType.RESOURCERESOLVER_MOCK
        || context.resourceResolverType() == ResourceResolverType.RESOURCEPROVIDER_MOCK) {
      assertNull(model.getCurrentNode());
      assertNull(model.getcurrentSession());
    }
    else {
      assertNotNull(model.getCurrentNode());
      assertNotNull(model.getcurrentSession());
    }

    assertNotNull(model.getComponentContext());
    assertNull(model.getEditContext());
    assertNotNull(model.getProperties());
    assertNotNull(model.getPageManager());
    assertNotNull(model.getCurrentPage());
    assertNotNull(model.getResourcePage());
    assertNotNull(model.getPageProperties());
    assertNotNull(model.getComponent());
    assertNotNull(model.getDesigner());
    assertNotNull(model.getCurrentDesign());
    assertNotNull(model.getResourceDesign());
    assertNotNull(model.getCurrentStyle());
    assertNotNull(model.getXssAPI());
    assertNull(model.getCurrentContentPolicy());
    assertNull(model.getCurrentContentPolicyProperties());
  }

  @Test
  public void testBindings_resourcePage() {
    // set the current resource
    context.currentResource(currentResource);

    // get the model
    SlingHttpServletRequest request = context.request();
    SlingBindingsModel model = request.adaptTo(SlingBindingsModel.class);

    // assert the model returns the correct page, resource, and resourcePage
    assertNotNull(model);
    assertEquals(this.currentPage, model.getCurrentPage());
    assertEquals(this.currentResource, model.getResource());
    assertEquals(this.currentPage, model.getResourcePage());

    // create a new sibling page
    Page page2 = context.create().page("/content/testPage2");
    Resource resourcePage2 = context.create().resource(page2.getContentResource().getPath() + "/testResource",
        "sling:resourceType", COMPONENT_RESOURCE_TYPE);

    // use the model factory to wrap the request to get the model for the resource on the NEW page
    ModelFactory modelFactory = context.getService(ModelFactory.class);
    SlingBindingsModel model2 = modelFactory.getModelFromWrappedRequest(request, resourcePage2, SlingBindingsModel.class);

    // assert the new model is as expected
    assertNotNull(model2);
    assertEquals(this.currentPage.getPath(), model2.getCurrentPage().getPath());
    assertEquals(resourcePage2.getPath(), model2.getResource().getPath());
    // this test confirms that the "resourcePage" is the page that contains the "resource"
    assertEquals(page2.getPath(), model2.getResourcePage().getPath());
  }

  @Test
  public void testBindings_EditMode() {
    WCMMode.EDIT.toRequest(context.request());
    context.currentResource(currentResource);

    SlingBindingsModel model = context.request().adaptTo(SlingBindingsModel.class);

    assertNotNull(model);

    assertNotNull(model.getResolver());
    assertNotNull(model.getResource());
    assertNotNull(model.getRequest());
    assertNotNull(model.getResponse());
    if (context.resourceResolverType() == ResourceResolverType.RESOURCERESOLVER_MOCK
        || context.resourceResolverType() == ResourceResolverType.RESOURCEPROVIDER_MOCK) {
      assertNull(model.getCurrentNode());
      assertNull(model.getcurrentSession());
    }
    else {
      assertNotNull(model.getCurrentNode());
      assertNotNull(model.getcurrentSession());
    }

    assertNotNull(model.getComponentContext());
    assertNotNull(model.getEditContext());
    assertNotNull(model.getProperties());
    assertNotNull(model.getPageManager());
    assertNotNull(model.getCurrentPage());
    assertNotNull(model.getResourcePage());
    assertNotNull(model.getPageProperties());
    assertNotNull(model.getComponent());
    assertNotNull(model.getDesigner());
    assertNotNull(model.getCurrentDesign());
    assertNotNull(model.getResourceDesign());
    assertNotNull(model.getCurrentStyle());
    assertNotNull(model.getXssAPI());
    assertNull(model.getCurrentContentPolicy());
    assertNull(model.getCurrentContentPolicyProperties());
  }

  @Test
  public void testBindingsModelFactory() throws Exception {
    context.currentResource(currentResource);

    ModelFactory modelFactory = context.getService(ModelFactory.class);
    SlingBindingsModel model = modelFactory.getModelFromWrappedRequest(context.request(), context.currentResource(), SlingBindingsModel.class);

    assertNotNull(model);

    assertNotNull(model.getResolver());
    assertNotNull(model.getResource());
    assertNotNull(model.getRequest());
    assertNotNull(model.getResponse());
    if (context.resourceResolverType() == ResourceResolverType.RESOURCERESOLVER_MOCK
        || context.resourceResolverType() == ResourceResolverType.RESOURCEPROVIDER_MOCK) {
      assertNull(model.getCurrentNode());
      assertNull(model.getcurrentSession());
    }
    else {
      assertNotNull(model.getCurrentNode());
      assertNotNull(model.getcurrentSession());
    }

    assertNotNull(model.getComponentContext());
    assertNull(model.getEditContext());
    assertNotNull(model.getProperties());
    assertNotNull(model.getPageManager());
    assertNotNull(model.getCurrentPage());
    assertNotNull(model.getResourcePage());
    assertNotNull(model.getPageProperties());
    assertNotNull(model.getComponent());
    assertNotNull(model.getDesigner());
    assertNotNull(model.getCurrentDesign());
    assertNotNull(model.getResourceDesign());
    assertNotNull(model.getCurrentStyle());
    assertNotNull(model.getXssAPI());
    assertNull(model.getCurrentContentPolicy());
    assertNull(model.getCurrentContentPolicyProperties());
  }

  @Test
  public void testContentPolicy() {
    context.contentPolicyMapping(COMPONENT_RESOURCE_TYPE,
        "policyProp1", "value1");

    context.currentResource(currentResource);

    SlingBindingsModel model = context.request().adaptTo(SlingBindingsModel.class);
    assertEquals("value1", model.getCurrentStyle().get("policyProp1", String.class));
    assertEquals("value1", model.getCurrentContentPolicy().getProperties().get("policyProp1", String.class));
    assertEquals("value1", model.getCurrentContentPolicyProperties().get("policyProp1", String.class));
  }

  @Test
  public void testContentPolicy_SlingModelDelegation() {
    context.contentPolicyMapping(COMPONENT_RESOURCE_TYPE,
        "policyProp1", "value1");

    // wrap current resource with resource type forced to resource super type
    // to simulate Sling Model Delegation Pattern
    context.currentResource(new ResourceTypeForcingResourceWrapper(currentResource, COMPONENT_RESOURCE_SUPER_TYPE));

    SlingBindingsModel model = context.request().adaptTo(SlingBindingsModel.class);
    assertEquals("value1", model.getCurrentStyle().get("policyProp1", String.class));
    assertNull(model.getCurrentContentPolicy());
    assertNull(model.getCurrentContentPolicyProperties());
  }

  @Test
  public void testContentPolicy_CompositeSlingModelDelegationViaRequestWrapper() {
    context.currentResource(currentResource);
    context.create().resource("/apps/" + CHILD_COMPOSITE_COMPONENT_RESOURCE_TYPE,
                              JcrConstants.JCR_PRIMARYTYPE, NameConstants.NT_COMPONENT);
    context.contentPolicyMapping(CHILD_COMPOSITE_COMPONENT_RESOURCE_TYPE,
                                 "policyProp1", "value1");
    AdapterManager adapterManager = context.getService(AdapterManager.class);
    BindingsValuesProvidersByContext bindingsValuesProvidersByContext = context.getService(BindingsValuesProvidersByContext.class);
    Resource testResource = context.create().resource(currentPage.getContentResource().getPath() + "/testResource/child1",
                                                      "sling:resourceType", CHILD_COMPOSITE_COMPONENT_RESOURCE_TYPE);

    // wrap current request with resource
    SlingHttpServletRequest request = new MockOverrideSlingPathRequestWrapper(adapterManager, context.request(), testResource.getPath(),
                                                                              bindingsValuesProvidersByContext);

    SlingBindingsModel model = request.adaptTo(SlingBindingsModel.class);
    assertEquals("value1", model.getCurrentStyle().get("policyProp1", String.class));
  }

}
