/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2024 wcm.io
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

import java.util.Objects;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;

import com.adobe.granite.asset.api.Asset;
import com.adobe.granite.asset.api.AssetManager;
import com.day.cq.dam.api.DamConstants;

/**
 * Mock implementation of Adobe Granite {@link AssetManager}. This is done by wrapping a {@link MockAssetManager}.
 */
public class MockGraniteAssetManagerWrapper implements AssetManager {

  private final ResourceResolver resourceResolver;
  private final com.day.cq.dam.api.AssetManager cqAssetManager;

  MockGraniteAssetManagerWrapper(@NotNull ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
    this.cqAssetManager = resourceResolver.adaptTo(com.day.cq.dam.api.AssetManager.class);
  }

  @Override
  public Asset createAsset(String s) {
    com.day.cq.dam.api.Asset asset = cqAssetManager.createAsset(s, null, null, false);
    return new MockGraniteAssetWrapper(asset);
  }

  @Override
  public Asset getAsset(String s) {
    Resource resource = resourceResolver.getResource(s);
    return resource != null ? resource.adaptTo(Asset.class) : null;
  }

  @Override
  public boolean assetExists(String s) {
    Resource assetResource = resourceResolver.getResource(s);
    return assetResource != null && Objects.equals(DamConstants.NT_DAM_ASSET, assetResource.getValueMap().get(JcrConstants.JCR_PRIMARYTYPE, String.class));
  }

  @Override
  @SuppressWarnings("java:S112") // allow throwing RuntimException
  public void removeAsset(String s) {
    try {
      Resource assetResource = resourceResolver.getResource(s);
      if (assetResource != null) {
        resourceResolver.delete(assetResource);
      }
    }
    catch (PersistenceException pe) {
      throw new RuntimeException(pe);
    }
  }


  // --- unsupported operations ---

  @Override
  public Asset getAssetByIdentifier(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void copyAsset(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void moveAsset(String s, String s1) {
    throw new UnsupportedOperationException();
  }
}
