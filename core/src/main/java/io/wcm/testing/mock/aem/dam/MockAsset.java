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
package io.wcm.testing.mock.aem.dam;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Binary;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import com.adobe.granite.asset.api.RenditionHandler;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.DamEvent;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.RenditionPicker;
import com.day.cq.dam.api.Revision;

/**
 * Mock implementation of {@link Asset}.
 */
@SuppressWarnings({
    "null",
    "java:S112" // allow throwing RuntimException
})
class MockAsset extends SlingAdaptable implements Asset {

  private final ResourceResolver resourceResolver;
  protected final Resource resource;
  private final ValueMap contentProps;
  private final Resource renditionsResource;
  private final EventAdmin eventAdmin;
  private final BundleContext bundleContext;
  private boolean batchMode;

  MockAsset(@NotNull Resource resource, EventAdmin eventAdmin, BundleContext bundleContext) {
    this.resourceResolver = resource.getResourceResolver();
    this.resource = resource;
    Resource contentResource = resource.getChild(JcrConstants.JCR_CONTENT);
    this.contentProps = ResourceUtil.getValueMap(contentResource);
    this.renditionsResource = resource.getChild(JcrConstants.JCR_CONTENT + "/" + DamConstants.RENDITIONS_FOLDER);
    this.eventAdmin = eventAdmin;
    this.bundleContext = bundleContext;
  }

  @Override
  public <AdapterType> AdapterType adaptTo(@NotNull Class<AdapterType> type) {
    if (type == Resource.class) {
      return type.cast(resource);
    }
    //to be able to adapt to granite asset
    if (type == com.adobe.granite.asset.api.Asset.class) {
      return type.cast(new MockGraniteAssetWrapper(this, resource));
    }
    return super.adaptTo(type);
  }

  @Override
  public Map<String, Object> getMetadata() {
    Resource metadataResource = resource.getChild(JcrConstants.JCR_CONTENT + "/" + DamConstants.METADATA_FOLDER);
    return ResourceUtil.getValueMap(metadataResource);
  }

  @Override
  public Object getMetadata(String name) {
    return getMetadata().get(name);
  }

  @Override
  public String getPath() {
    return resource.getPath();
  }

  @Override
  public String getName() {
    return resource.getName();
  }

  @Override
  public String getMetadataValue(String name) {
    Object value = getMetadata(name);
    if (value == null) {
      return "";
    }
    else {
      return value.toString();
    }
  }

  @Override
  public String getMetadataValueFromJcr(String name) {
    return getMetadataValue(name);
  }

  @Override
  public long getLastModified() {
    Calendar lastModified = contentProps.get(JcrConstants.JCR_LASTMODIFIED, Calendar.class);
    if (lastModified != null) {
      return lastModified.getTimeInMillis();
    }
    else {
      return 0L;
    }
  }

  @Override
  public String getModifier() {
    return contentProps.get(JcrConstants.JCR_LAST_MODIFIED_BY, "");
  }

  @Override
  public String getMimeType() {
    String mimeType = getMetadataValue(DamConstants.DC_FORMAT);
    if (StringUtils.isBlank(mimeType)) {
      Rendition original = getOriginal();
      if (original != null) {
        mimeType = original.getMimeType();
      }
    }
    return mimeType;
  }

  @Override
  public List<Rendition> getRenditions() {
    return IteratorUtils.toList(listRenditions());
  }

  @Override
  public Iterator<Rendition> listRenditions() {
    if (this.renditionsResource == null) {
      return Collections.emptyIterator();
    }
    Iterator<Resource> renditionResources = this.resourceResolver.listChildren(this.renditionsResource);
    return ResourceUtil.adaptTo(renditionResources, Rendition.class);
  }

  @Override
  public Rendition getRendition(String name) {
    Iterator<Rendition> renditions = listRenditions();
    while (renditions.hasNext()) {
      Rendition rendition = renditions.next();
      if (StringUtils.equals(rendition.getName(), name)) {
        return rendition;
      }
    }
    return null;
  }

  @Override
  public Rendition getRendition(RenditionPicker picker) {
    return picker.getRendition(this);
  }

  @Override
  public Rendition getOriginal() {
    return getRendition(DamConstants.ORIGINAL_FILE);
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MockAsset)) {
      return false;
    }
    return StringUtils.equals(getPath(), ((MockAsset)obj).getPath());
  }

  @Override
  public Rendition addRendition(String name, InputStream is, String mimeType) {
    if (getRendition(name) != null) {
      removeRendition(name);
    }
    ContentLoader contentLoader = new ContentLoader(resourceResolver, bundleContext, false);
    Resource rendition = contentLoader.binaryFile(is, renditionsResource.getPath() + "/" + name, mimeType);
    try {
      if (!isBatchMode()) {
        resourceResolver.commit();
      }
    }
    catch (PersistenceException ex) {
      throw new RuntimeException("Unable to remove resource: " + rendition.getPath(), ex);
    }

    // send DamEvent after rendition creation
    eventAdmin.sendEvent(DamEvent.renditionUpdated(getPath(), resourceResolver.getUserID(), rendition.getPath()).toEvent());

    return rendition.adaptTo(Rendition.class);
  }

  @Override
  public void removeRendition(String name) {
    Resource rendition = renditionsResource.getChild(name);
    if (rendition == null) {
      // rendition does not exist, do nothing
      return;
    }

    try {
      resourceResolver.delete(rendition);
      if (!isBatchMode()) {
        resourceResolver.commit();
      }
    }
    catch (PersistenceException ex) {
      throw new RuntimeException("Unable to remove resource: " + rendition.getPath(), ex);
    }

    // send DamEvent after rendition creation
    eventAdmin.sendEvent(DamEvent.renditionRemoved(getPath(), resourceResolver.getUserID(), rendition.getPath()).toEvent());
  }

  @Override
  public Resource setRendition(String name, InputStream is, String mimeType) {
    return addRendition(name, is, mimeType);
  }

  @Override
  public String getID() {
    return resource.getValueMap().get(JcrConstants.JCR_UUID, "");
  }

  @Override
  public Rendition addRendition(String name, InputStream is, Map<String, Object> map) {
    Object mimeTypeObject = map.get(RenditionHandler.PROPERTY_RENDITION_MIME_TYPE);
    if (mimeTypeObject instanceof String) {
      return addRendition(name, is, mimeTypeObject.toString());
    }
    throw new UnsupportedOperationException("Mime type property missing in map: " + RenditionHandler.PROPERTY_RENDITION_MIME_TYPE);
  }


  // --- unsupported operations ---

  @Override
  public Rendition getCurrentOriginal() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSubAsset() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setCurrentOriginal(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Revision createRevision(String label, String comment) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Asset restore(String revisionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Revision> getRevisions(Calendar cal) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Asset addSubAsset(String name, String mimeType, InputStream stream) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Asset> getSubAssets() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBatchMode(boolean mode) {
    this.batchMode = mode;
  }

  @Override
  public boolean isBatchMode() {
    return this.batchMode;
  }

  @Override
  public Rendition getImagePreviewRendition() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void initAssetState() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Rendition addRendition(String name, Binary binary, String mimeType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Rendition addRendition(String name, Binary binary, Map<String, Object> arg2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Revision createRevision(String name, String binary, User arg2) {
    throw new UnsupportedOperationException();
  }

  // AEM Cloud
  @SuppressWarnings("unused")
  public Rendition setRendition(String name, Binary binary, String mimeType) {
    throw new UnsupportedOperationException();
  }

}
