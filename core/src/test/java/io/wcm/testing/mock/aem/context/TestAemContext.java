/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 - 2018 wcm.io
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

import java.io.IOException;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.testing.mock.sling.NodeTypeDefinitionScanner;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.jetbrains.annotations.NotNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;
import io.wcm.testing.mock.aem.junit.AemContextCallback;

public final class TestAemContext {

  /**
   * All resource resolver types the unit tests of aem-mock should run with.
   */
  @SuppressFBWarnings("MS_PKGPROTECT")
  public static final ResourceResolverType[] ALL_TYPES = new @NotNull ResourceResolverType[] {
      ResourceResolverType.JCR_MOCK,
      ResourceResolverType.RESOURCERESOLVER_MOCK,
      ResourceResolverType.RESOURCEPROVIDER_MOCK,
      ResourceResolverType.JCR_OAK
  };

  private TestAemContext() {
    // static methods only
  }

  public static @NotNull AemContext newAemContext() {
    return newAemContext(ALL_TYPES);
  }

  public static @NotNull AemContext newAemContext(ResourceResolverType... resourceResolverTypes) {
    return new AemContext(new SetUpCallback(), resourceResolverTypes);
  }

  public static @NotNull AemContextBuilder newAemContextBuilder() {
    return newAemContextBuilder(ALL_TYPES);
  }

  public static @NotNull AemContextBuilder newAemContextBuilder(ResourceResolverType... resourceResolverTypes) {
    return new AemContextBuilder(resourceResolverTypes)
        .afterSetUp(new SetUpCallback());
  }

  /**
   * Custom set up rules required in all unit tests.
   */
  private static final class SetUpCallback implements AemContextCallback {

    @Override
    public void execute(@NotNull AemContext context) throws PersistenceException, IOException {
      try {
        // register manually because in project's unit tests itself MANIFEST.MF is not available
        NodeTypeDefinitionScanner.get().register(context.resourceResolver().adaptTo(Session.class),
            List.of("SLING-INF/nodetypes/aem-core-replication.cnd",
                "SLING-INF/nodetypes/aem-tagging.cnd",
                "SLING-INF/nodetypes/aem-commons.cnd",
                "SLING-INF/nodetypes/aem-dam.cnd",
                "SLING-INF/nodetypes/aem-dam1.cnd",
                "SLING-INF/nodetypes/vlt.cnd"),
                context.resourceResolverType().getNodeTypeMode());
      }
      catch (RepositoryException ex) {
        throw new RuntimeException("Unable to register AEM nodetypes: " + ex.getMessage(), ex);
      }
    }

  }

}
