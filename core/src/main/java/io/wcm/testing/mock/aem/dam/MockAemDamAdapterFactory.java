/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.testing.mock.aem.dam;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventAdmin;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;

/**
 * Mock adapter factory for AEM Asset-related adaptions.
 */
@Component(service = AdapterFactory.class,
    property = {
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.ResourceResolver",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.dam.api.Asset",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.dam.api.AssetManager",
        AdapterFactory.ADAPTER_CLASSES + "=com.day.cq.dam.api.Rendition",
        AdapterFactory.ADAPTER_CLASSES + "=com.adobe.granite.asset.api.Asset",
        AdapterFactory.ADAPTER_CLASSES + "=com.adobe.granite.asset.api.AssetManager",
        AdapterFactory.ADAPTER_CLASSES + "=com.adobe.granite.asset.api.Rendition"
    })
@ProviderType
public final class MockAemDamAdapterFactory implements AdapterFactory {

  @Reference
  private EventAdmin eventAdmin;

  private BundleContext bundleContext;

  @Activate
  private void activate(BundleContext context) {
    this.bundleContext = context;
  }

  @Override
  public @Nullable <AdapterType> AdapterType getAdapter(final @NotNull Object adaptable, final @NotNull Class<AdapterType> type) {
    if (adaptable instanceof Resource) {
      return getAdapter((Resource)adaptable, type);
    }
    if (adaptable instanceof ResourceResolver) {
      return getAdapter((ResourceResolver)adaptable, type);
    }
    return null;
  }

  private @Nullable <AdapterType> AdapterType getAdapter(@NotNull final Resource resource, @NotNull final Class<AdapterType> type) {
    if (DamUtil.isAsset(resource)) {
      if (type == com.adobe.granite.asset.api.Asset.class) {
        return type.cast(new MockGraniteAssetWrapper(new MockAsset(resource, eventAdmin, bundleContext), resource));
      }
      else if (type == Asset.class) {
        return type.cast(new MockAsset(resource, eventAdmin, bundleContext));
      }
    }
    if ((type == Rendition.class || type == com.adobe.granite.asset.api.Rendition.class) && DamUtil.isRendition(resource)) {
      return type.cast(new MockRendition(resource));
    }
    return null;
  }

  private @Nullable <AdapterType> AdapterType getAdapter(@NotNull final ResourceResolver resolver, @NotNull final Class<AdapterType> type) {
    if (type == AssetManager.class) {
      return type.cast(new MockAssetManager(resolver, eventAdmin, bundleContext));
    }
    else if (type == com.adobe.granite.asset.api.AssetManager.class) {
      return type.cast(new MockGraniteAssetManagerWrapper(resolver));
    }
    return null;
  }

}
