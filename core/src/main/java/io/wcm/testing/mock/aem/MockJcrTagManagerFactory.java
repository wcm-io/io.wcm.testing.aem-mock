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
