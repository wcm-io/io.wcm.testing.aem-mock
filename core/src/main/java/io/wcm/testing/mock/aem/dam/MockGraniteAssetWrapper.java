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

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Binary;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;

import com.adobe.granite.asset.api.Asset;
import com.adobe.granite.asset.api.AssetMetadata;
import com.adobe.granite.asset.api.AssetRelation;
import com.adobe.granite.asset.api.Rendition;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Mock implementation of Adobe Granite {@link Asset}. This is done by wrapping a {@link MockAsset}
 */
@SuppressWarnings("null")
public class MockGraniteAssetWrapper extends ResourceWrapper implements Asset {

  private final com.day.cq.dam.api.Asset asset;

  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE") // adaption to Resource will always work
  MockGraniteAssetWrapper(com.day.cq.dam.api.Asset asset) {
    super(asset.adaptTo(Resource.class));
    this.asset = asset;
  }

  @Override
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    //to be able to adapt to CQ asset
    if (type == com.day.cq.dam.api.Asset.class) {
      return type.cast(asset);
    }
    return asset.adaptTo(type);
  }

  @Override
  public Rendition getRendition(String s) {
    return (Rendition)asset.getRendition(s);
  }

  @Override
  public Iterator<? extends Rendition> listRenditions() {
    List<Rendition> graniteRenditions = new LinkedList<>();
    Iterator<com.day.cq.dam.api.Rendition> renditions = asset.listRenditions();
    while (renditions.hasNext()) {
      graniteRenditions.add((Rendition)renditions.next());
    }
    return graniteRenditions.iterator();
  }

  @Override
  public String getIdentifier() {
    return asset.getID();
  }

  @Override
  public Rendition setRendition(String name, InputStream inputStream, Map<String, Object> map) {
    return (Rendition)asset.addRendition(name, inputStream, map);
  }

  @Override
  public void removeRendition(String s) {
    asset.removeRendition(s);
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MockGraniteAssetWrapper)) {
      return false;
    }
    return StringUtils.equals(getPath(), ((MockGraniteAssetWrapper)obj).getPath());
  }


  // --- unsupported operations ---

  @Override
  public Iterator<? extends Asset> listRelated(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<? extends AssetRelation> listRelations(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AssetMetadata getAssetMetadata() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Rendition setRendition(String s, Binary binary, Map<String, Object> map) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AssetRelation addRelation(String s, String s1, Map<String, Object> map) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setRelation(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AssetRelation addRelation(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void orderRelationBefore(String s, String s1, String s2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeRelation(String s, String s1) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeRelation(String s) {
    throw new UnsupportedOperationException();
  }

}
