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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.binary.BinaryDownload;
import org.apache.jackrabbit.api.binary.BinaryDownloadOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;

/**
 * Mock implementation of {@link Rendition} and {@link com.adobe.granite.asset.api.Rendition}.
 */
@SuppressWarnings({
    "null",
    "java:S112" // allow throwing RuntimException
})
class MockRendition extends ResourceWrapper implements Rendition, com.adobe.granite.asset.api.Rendition {

  private final Resource resource;
  private final Resource contentResource;
  private final ValueMap contentProps;

  MockRendition(@NotNull Resource resource) {
    super(resource);
    this.resource = resource;
    this.contentResource = resource.getChild(JcrConstants.JCR_CONTENT);
    this.contentProps = ResourceUtil.getValueMap(this.contentResource);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == Resource.class) {
      return (AdapterType)resource;
    }
    //to be able to adapt to granite rendition and back
    if (type == Rendition.class || type == com.adobe.granite.asset.api.Rendition.class) {
      return (AdapterType)this;
    }
    return super.adaptTo(type);
  }

  @Override
  public ValueMap getProperties() {
    return this.contentProps;
  }

  @Override
  public String getMimeType() {
    return this.contentProps.get(JcrConstants.JCR_MIMETYPE, String.class);
  }

  @Override
  public InputStream getStream() {
    Resource data = contentResource.getChild(JcrConstants.JCR_DATA);
    if (data != null) {
      return data.adaptTo(InputStream.class);
    }
    else {
      return null;
    }
  }

  @Override
  public long getSize() {
    try {
      InputStream is = getStream();
      if (is == null) {
        return 0L;
      }
      return IOUtils.toByteArray(is).length;
    }
    catch (IOException ex) {
      throw new RuntimeException("Unable to read binary data: " + getPath(), ex);
    }
  }

  @Override
  public Asset getAsset() {
    return DamUtil.resolveToAsset(this.resource);
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MockRendition)) {
      return false;
    }
    return StringUtils.equals(getPath(), ((MockRendition)obj).getPath());
  }


  // --- unsupported operations ---

  @Override
  public Binary getBinary() {
    return new MockBinary(this);
  }


  private static class MockBinary implements BinaryDownload {

    private Rendition rendition;

    MockBinary(Rendition rendition) {
      this.rendition = rendition;
    }


    @Override
    public InputStream getStream() throws RepositoryException {
      return rendition.getStream();
    }

    @Override
    public int read(byte[] b, long position) throws IOException, RepositoryException {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getSize() throws RepositoryException {
      return rendition.getSize();
    }

    @Override
    public void dispose() {
      // nothing to do
    }

    @Override
    public @Nullable URI getURI(BinaryDownloadOptions downloadOptions) throws RepositoryException {
      final String path = "https://blostore.local:12345/blostore/" + rendition.getPath();
      try {
        return new URI(path);
      }
      catch (URISyntaxException e) {
        // nothing
      }
      return null;
    }

  }


}
