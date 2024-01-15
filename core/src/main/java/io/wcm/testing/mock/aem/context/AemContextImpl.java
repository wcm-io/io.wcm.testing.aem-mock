/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.osgi.MapUtil;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.context.SlingContextImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.dam.api.AssetManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.policies.ContentPolicyMapping;
import com.day.cq.wcm.commons.WCMUtils;

import io.wcm.testing.mock.aem.MockAemAdapterFactory;
import io.wcm.testing.mock.aem.MockComponentContext;
import io.wcm.testing.mock.aem.MockContentPolicyStorage;
import io.wcm.testing.mock.aem.MockExternalizer;
import io.wcm.testing.mock.aem.MockJcrTagManagerFactory;
import io.wcm.testing.mock.aem.MockLanguageManager;
import io.wcm.testing.mock.aem.MockLayerAdapterFactory;
import io.wcm.testing.mock.aem.MockPageManagerFactory;
import io.wcm.testing.mock.aem.MockSlingModelFilter;
import io.wcm.testing.mock.aem.builder.ContentBuilder;
import io.wcm.testing.mock.aem.dam.MockAemDamAdapterFactory;
import io.wcm.testing.mock.aem.dam.MockAssetHandler;
import io.wcm.testing.mock.aem.dam.MockAssetStore;
import io.wcm.testing.mock.aem.dam.MockPublishUtils;
import io.wcm.testing.mock.aem.granite.MockResourceCollectionManager;
import io.wcm.testing.mock.aem.xf.MockExperienceFragmentAdapterFactory;

/**
 * Defines AEM context objects with lazy initialization.
 * Should not be used directly but via the JUnit 4 rule or JUnit 5 extension.
 */
@ConsumerType
public class AemContextImpl extends SlingContextImpl {

  // default to publish instance run mode
  static final Set<String> DEFAULT_RUN_MODES = Collections.singleton("publish");

  @Override
  protected void registerDefaultServices() {
    // register default services from osgi-mock and sling-mock
    super.registerDefaultServices();

    // adapter factories
    registerInjectActivateService(new MockAemAdapterFactory());
    registerInjectActivateService(new MockAemDamAdapterFactory());
    registerInjectActivateService(new MockLayerAdapterFactory());
    registerInjectActivateService(new MockExperienceFragmentAdapterFactory());

    // other services
    registerInjectActivateService(new MockAssetHandler());
    registerInjectActivateService(new MockAssetStore());
    registerInjectActivateService(new MockPublishUtils());
    registerInjectActivateService(new MockAemBindingsValuesProvider(),
        MockAemBindingsValuesProvider.PROPERTY_CONTEXT, this);
    registerInjectActivateService(new MockPageManagerFactory());
    registerInjectActivateService(new MockLanguageManager());
    registerInjectActivateService(new MockResourceCollectionManager());
    registerInjectActivateService(new MockSlingModelFilter());
    registerInjectActivateService(new MockExternalizer());
    registerInjectActivateService(new MockJcrTagManagerFactory());
  }

  @Override
  protected void setResourceResolverType(@Nullable ResourceResolverType resourceResolverType) {
    super.setResourceResolverType(resourceResolverType);
  }

  @Override
  protected @NotNull ResourceResolverFactory newResourceResolverFactory() {
    return ContextResourceResolverFactory.get(this.resourceResolverType, bundleContext());
  }

  @Override
  protected void setUp() {
    super.setUp();
  }

  @Override
  protected void tearDown() {
    super.tearDown();
  }

  /**
   * Merges the given custom Resource Resolver Factory Activator OSGi configuration with the default configuration
   * applied in AEM 6. The custom configuration has higher precedence.
   * @param customProps Custom config
   * @return Merged config
   */
  protected final Map<String, Object> resourceResolverFactoryActivatorPropsMergeWithAemDefault(@Nullable Map<String, Object> customProps) {
    Map<String, Object> props = new HashMap<>();

    props.put("resource.resolver.searchpath", new String[] {
        "/apps",
        "/libs",
        "/apps/foundation/components/primary",
        "/libs/foundation/components/primary",
    });
    props.put("resource.resolver.manglenamespaces", true);
    props.put("resource.resolver.allowDirect", true);
    props.put("resource.resolver.virtual", new String[] {
        "/:/"
    });
    props.put("resource.resolver.mapping", new String[] {
        "/-/"
    });
    props.put("resource.resolver.map.location", "/etc/map");
    props.put("resource.resolver.default.vanity.redirect.status", "302");
    props.put("resource.resolver.vanitypath.maxEntries", -1);
    props.put("resource.resolver.vanitypath.bloomfilter.maxBytes", 1024000);
    props.put(ResourceResolverFactoryConfigPropertyNames.getVanityPathAllowListPropertyName(), new String[] {
        "/apps/",
        "/libs/",
        "/content/"
    });
    props.put(ResourceResolverFactoryConfigPropertyNames.getVanityPathDenyListPropertyName(), new String[] {
        "/content/usergenerated"
    });
    props.put("resource.resolver.vanity.precedence", false);
    props.put("resource.resolver.providerhandling.paranoid", false);

    if (customProps != null) {
      props.putAll(customProps);
    }

    return props;
  }

  /**
   * @return Page manager
   */
  public @NotNull PageManager pageManager() {
    PageManager pageManager = resourceResolver().adaptTo(PageManager.class);
    if (pageManager == null) {
      throw new RuntimeException("No page manager.");
    }
    return pageManager;
  }

  /**
   * @return Asset manager
   */
  public @NotNull AssetManager assetManager() {
    AssetManager assetManager = resourceResolver().adaptTo(AssetManager.class);
    if (assetManager == null) {
      throw new RuntimeException("No asset manager");
    }
    return assetManager;
  }

  /**
   * @return Content builder for building test content
   */
  @Override
  public @NotNull ContentBuilder create() {
    if (this.contentBuilder == null) {
      this.contentBuilder = new ContentBuilder(this);
    }
    return (ContentBuilder)this.contentBuilder;
  }

  @Override
  public @Nullable Resource currentResource(@Nullable Resource resource) {
    Resource result = super.currentResource(resource);
    if (!hasWcmComponentContext()) {
      setCurrentPageInWcmComponentContext(currentPage());
    }
    return result;
  }

  /**
   * @return Current page from {@link ComponentContext}. If none is set the page containing the current resource.
   *         Null if no containing page exists.
   */
  public @Nullable Page currentPage() {
    ComponentContext context = WCMUtils.getComponentContext(request());
    if (context != null) {
      return context.getPage();
    }
    if (currentResource() != null) {
      return pageManager().getContainingPage(currentResource());
    }
    return null;
  }

  /**
   * Set current Page in request (via {@link ComponentContext}).
   * This also sets the current resource to the content resource of the page.
   * You can set it to a different resources afterwards if required.
   * @param pagePath Page path
   * @return currentPage
   */
  public @Nullable Page currentPage(@Nullable String pagePath) {
    if (pagePath != null) {
      Page page = pageManager().getPage(pagePath);
      if (page == null) {
        throw new IllegalArgumentException("Page does not exist: " + pagePath);
      }
      return currentPage(page);
    }
    else {
      currentPage((Page)null);
      return null;
    }
  }

  /**
   * Set current Page in request (via {@link ComponentContext}).
   * This also sets the current resource to the content resource of the page.
   * You can set it to a different resources afterwards if required.
   * @param page Page
   * @return currentPage
   */
  public @Nullable Page currentPage(@Nullable Page page) {
    if (page != null) {
      currentResource(page.getContentResource());
    }
    else {
      currentResource((Resource)null);
    }
    setCurrentPageInWcmComponentContext(page);
    return page;
  }

  private void setCurrentPageInWcmComponentContext(Page page) {
    ComponentContext wcmComponentContext = null;
    if (page != null) {
      wcmComponentContext = new MockComponentContext(page, request());
    }
    request().setAttribute(ComponentContext.CONTEXT_ATTR_NAME, wcmComponentContext);
  }

  private boolean hasWcmComponentContext() {
    return request().getAttribute(ComponentContext.CONTEXT_ATTR_NAME) != null;
  }

  /**
   * Create unique root paths for unit tests (and clean them up after the test run automatically).
   * @return Unique root path helper
   */
  @Override
  public @NotNull UniqueRoot uniqueRoot() {
    if (uniqueRoot == null) {
      uniqueRoot = new UniqueRoot(this);
    }
    return (UniqueRoot)uniqueRoot;
  }

  /**
   * Creates a mocked content policy with the given properties and maps it to all content resources with the given
   * resource type. This is a shortcut to easily test your components with a content policy.
   * @param resourceType Resource type that should be mapped to the content policy
   * @param properties Properties for the content policy
   * @return New content policy mapping
   */
  public @NotNull ContentPolicyMapping contentPolicyMapping(@NotNull String resourceType, Map<String, Object> properties) {
    return MockContentPolicyStorage.storeContentPolicyMapping(resourceType, properties, resourceResolver());
  }

  /**
   * Creates a mocked content policy with the given properties and maps it to all content resources with the given
   * resource type. This is a shortcut to easily test your components with a content policy.
   * @param resourceType Resource type that should be mapped to the content policy
   * @param properties Properties for the content policy
   * @return New content policy mapping
   */
  public @NotNull ContentPolicyMapping contentPolicyMapping(@NotNull String resourceType, @NotNull Object @NotNull... properties) {
    return contentPolicyMapping(resourceType, MapUtil.toMap(properties));
  }

  @Override
  protected @Nullable Object resolveSlingBindingProperty(@NotNull String property,
      @NotNull SlingHttpServletRequest bindingsContextRequest) {
    Object result = super.resolveSlingBindingProperty(property, bindingsContextRequest);
    if (result == null) {
      result = MockAemSlingBindings.resolveSlingBindingProperty(this, property, bindingsContextRequest);
    }
    return result;
  }

}
