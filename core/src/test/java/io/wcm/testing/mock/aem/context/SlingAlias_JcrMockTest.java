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

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;

public class SlingAlias_JcrMockTest {

  @Rule
  public AemContext context = new AemContextBuilder(ResourceResolverType.JCR_MOCK)
      /*
       * The optimized alias resolution is based on ResourceChangeListener, which is not supported with JCR_MOCK,
       * because JCR_MOCK does not support JCR Observation events. So, to use sling:alias with JCR_MOCK
       * we have to disabled the optimized alias resolution.
       * But be warned: This old code path is deprecated and likely to be removed in future Sling versions.
       * See SLING-12054 and SLING-12025 as references.
       */
      .resourceResolverFactoryActivatorProps(Map.of("resource.resolver.optimize.alias.resolution", false))
      .build();

  private String contentRoot;

  @Before
  public void setUp() throws Exception {
    contentRoot = context.uniqueRoot().content() + "/sample";
  }

  @Test
  public void testSlingAlias() throws PersistenceException {
    Resource resource = context.create().resource(contentRoot + "/myresource",
        JCR_PRIMARYTYPE, NT_UNSTRUCTURED,
        "sling:alias", "myalias");
    context.resourceResolver().commit();

    assertEquals(contentRoot + "/myresource", resource.getPath());
    assertEquals(contentRoot + "/myalias", context.resourceResolver().map(resource.getPath()));
  }

}
