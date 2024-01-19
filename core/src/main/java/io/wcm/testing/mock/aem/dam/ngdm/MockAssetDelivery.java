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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;

import com.adobe.cq.wcm.spi.AssetDelivery;
import com.day.cq.dam.api.Asset;

/**
 * Mock implementation of {@link AssetDelivery} for Web-Optimized Image Delivery.
 * <p>
 * As Asset ID a md5 hash of the path is used.
 * </p>
 * <p>
 * This OSGi service is not registered by default in AEM Mocks, as it is not available in all contexts
 * (e.g. not in AEM 6.5 and AEMaaCS SDK).
 * </p>
 */
@Component(service = AssetDelivery.class)
public final class MockAssetDelivery implements AssetDelivery {

  private static final String ASSET_DELIVERY_URL_PREFIX = "/adobe/dynamicmedia/deliver";

  static final String PARAM_PATH = "path";
  static final String PARAM_SEO_NAME = "seoname";
  static final String PARAM_FORMAT = "format";

  private static final Set<String> DISALLOWED_URL_PARAMS = Set.of(
      PARAM_PATH, PARAM_SEO_NAME, PARAM_FORMAT);

  @Override
  public @Nullable String getDeliveryURL(@NotNull Resource resource, @Nullable Map<String, Object> parameterMap) {
    if (parameterMap == null) {
      throw new IllegalArgumentException("No parameter map given.");
    }
    String path = getMandatoryStringParam(parameterMap, PARAM_PATH);
    String seoname = getMandatoryStringParam(parameterMap, PARAM_SEO_NAME);
    String format = getMandatoryStringParam(parameterMap, PARAM_FORMAT);

    String urlParams = parameterMap.entrySet().stream()
        .filter(entry -> !DISALLOWED_URL_PARAMS.contains(entry.getKey()) && entry.getValue() != null)
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));

    String assetId = getAssetId(path);

    StringBuilder sb = new StringBuilder();
    sb.append(ASSET_DELIVERY_URL_PREFIX)
        .append("/").append(assetId)
        .append("/").append(URLEncoder.encode(seoname, StandardCharsets.UTF_8))
        .append(".").append(URLEncoder.encode(format, StandardCharsets.UTF_8));
    if (!urlParams.isEmpty()) {
      sb.append("?").append(urlParams);
    }
    return sb.toString();
  }

  private static @NotNull String getMandatoryStringParam(@NotNull Map<String, Object> parameterMap, @NotNull String paramName) {
    Object value = parameterMap.get(paramName);
    if (value == null) {
      throw new IllegalArgumentException("Missing parameter: " + paramName);
    }
    return value.toString();
  }

  /**
   * Generate Asset ID for given asset.
   * @param asset Asset
   * @return MD5 hash of asset path
   */
  public static String getAssetId(@NotNull Asset asset) {
    return getAssetId(asset.getPath());
  }

  /**
   * Generate Asset ID for given path.
   * @param path Asset path
   * @return MD5 hash of asset path
   */
  public static String getAssetId(@NotNull String path) {
    return DigestUtils.md5Hex(path);
  }

}
