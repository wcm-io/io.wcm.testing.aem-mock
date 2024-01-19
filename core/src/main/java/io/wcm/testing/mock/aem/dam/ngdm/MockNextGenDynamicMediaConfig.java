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
package io.wcm.testing.mock.aem.dam.ngdm;

import org.osgi.service.component.annotations.Component;

import com.adobe.cq.ui.wcm.commons.config.NextGenDynamicMediaConfig;

/**
 * Mock implementation of {@link NextGenDynamicMediaConfig}.
 * <p>
 * This OSGi service is not registered by default in AEM Mocks, as it is not available in all contexts.
 * </p>
 */
@Component(service = NextGenDynamicMediaConfig.class)
public final class MockNextGenDynamicMediaConfig implements NextGenDynamicMediaConfig {

  @SuppressWarnings("java:S1075") // not a file system path
  static final String DEFAULT_IMAGE_DELIVERY_BASE_PATH = "/adobe/dynamicmedia/deliver/{asset-id}/{seo-name}.{format}";

  private boolean enabled;
  private String repositoryId;
  private String apiKey;
  private String env;
  private String imsOrg;
  private String imsEnv;
  private String imsClient;
  private String assetSelectorsJsUrl;
  private String imageDeliveryBasePath = DEFAULT_IMAGE_DELIVERY_BASE_PATH;
  private String videoDeliveryPath;
  private String assetOriginalBinaryDeliveryPath;
  private String assetMetadataPath;

  @Override
  public boolean enabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String getRepositoryId() {
    return repositoryId;
  }

  public void setRepositoryId(String repositoryId) {
    this.repositoryId = repositoryId;
  }

  @Override
  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  @Override
  public String getImsOrg() {
    return imsOrg;
  }

  public void setImsOrg(String imsOrg) {
    this.imsOrg = imsOrg;
  }

  @Override
  public String getImsEnv() {
    return imsEnv;
  }

  public void setImsEnv(String imsEnv) {
    this.imsEnv = imsEnv;
  }

  @Override
  public String getImsClient() {
    return imsClient;
  }

  public void setImsClient(String imsClient) {
    this.imsClient = imsClient;
  }

  @Override
  public String getAssetSelectorsJsUrl() {
    return assetSelectorsJsUrl;
  }

  public void setAssetSelectorsJsUrl(String assetSelectorsJsUrl) {
    this.assetSelectorsJsUrl = assetSelectorsJsUrl;
  }

  @Override
  public String getImageDeliveryBasePath() {
    return imageDeliveryBasePath;
  }

  public void setImageDeliveryBasePath(String imageDeliveryBasePath) {
    this.imageDeliveryBasePath = imageDeliveryBasePath;
  }

  @Override
  public String getVideoDeliveryPath() {
    return videoDeliveryPath;
  }

  public void setVideoDeliveryPath(String videoDeliveryPath) {
    this.videoDeliveryPath = videoDeliveryPath;
  }

  @Override
  public String getAssetOriginalBinaryDeliveryPath() {
    return assetOriginalBinaryDeliveryPath;
  }

  public void setAssetOriginalBinaryDeliveryPath(String assetOriginalBinaryDeliveryPath) {
    this.assetOriginalBinaryDeliveryPath = assetOriginalBinaryDeliveryPath;
  }

  @Override
  public String getAssetMetadataPath() {
    return assetMetadataPath;
  }

  public void setAssetMetadataPath(String assetMetadataPath) {
    this.assetMetadataPath = assetMetadataPath;
  }

}
