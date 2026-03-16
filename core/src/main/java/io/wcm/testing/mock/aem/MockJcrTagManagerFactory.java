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
package io.wcm.testing.mock.aem;

import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.annotations.Component;

import com.day.cq.tagging.JcrTagManagerFactory;
import com.day.cq.tagging.TagManager;

/**
 * Mock implementation of {@link JcrTagManagerFactory}.
 */
@Component(service = JcrTagManagerFactory.class)
@ProviderType
public final class MockJcrTagManagerFactory implements JcrTagManagerFactory {

  @Override
  public TagManager getTagManager(final Session session) {
    // Tried to implement this method by injecting the ResourceResolverFactory as an OSGi Service, but this is not possible, due to the fact that
    // ResourceResolverType.NONE doesn't register the service on the BundleContext at construction of the AemContext.
    // This method is deprecated and shouldn't be used anyway, so it shouldn't be a problem.
    throw new UnsupportedOperationException();
  }

  @Override
  public TagManager getTagManager(final ResourceResolver resourceResolver) {
    if (resourceResolver == null) {
      throw new IllegalArgumentException("ResourceResolver must not be null");
    }
    return new MockTagManager(resourceResolver);
  }

}
