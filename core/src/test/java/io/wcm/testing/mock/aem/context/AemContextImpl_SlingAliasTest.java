/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2023 wcm.io
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

import static com.day.cq.commons.jcr.JcrConstants.JCR_PRIMARYTYPE;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.junit.AemContext;

public class AemContextImpl_SlingAliasTest {

  @Rule
  public AemContext context = TestAemContext.newAemContextBuilder()
      .resourceResolverFactoryActivatorProps(Map.of("resource.resolver.optimize.alias.resolution", false))
      .build();

  private String contentRoot;

  @Before
  public void setUp() throws Exception {
    contentRoot = context.uniqueRoot().content() + "/sample";
  }

  @Test
  public void testSlingAlias() {
    if (context.resourceResolverType() == ResourceResolverType.RESOURCERESOLVER_MOCK) {
      // sling:alias is not supported for RESOURCERESOLVER_MOCK
      return;
    }
    Resource resource = context.create().resource(contentRoot + "/myresource",
        JCR_PRIMARYTYPE, NT_UNSTRUCTURED,
        "sling:alias", "myalias");
    assertEquals(contentRoot + "/myresource", resource.getPath());
    assertEquals(contentRoot + "/myalias", context.resourceResolver().map(resource.getPath()));
  }

}
