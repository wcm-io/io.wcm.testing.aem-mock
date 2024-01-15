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

/**
 * Mock implementation of {@link AssetDelivery}.
 * As Asset ID a md5 hash of the path is used.
 */
@Component(service = AssetDelivery.class)
public final class MockAssetDelivery implements AssetDelivery {

  private static final String ASSET_DELIVERY_URL_PREFIX = "/asset/delivery";

  static final String PARAM_PATH = "path";
  static final String PARAM_SEO_NAME = "seoname";
  static final String PARAM_FORMAT = "format";

  private static final Set<String> ALLOWED_URL_PARAMS = Set.of(
      "width",
      "quality",
      "c",
      "r",
      "flip",
      "sz",
      "preferwebp"
  );

  @Override
  public @Nullable String getDeliveryURL(@NotNull Resource resource, @Nullable Map<String, Object> parameterMap) {
    if (parameterMap == null) {
      throw new IllegalArgumentException("No parameter map given.");
    }
    String path = getMandatoryStringParam(parameterMap, PARAM_PATH);
    String seoname = getMandatoryStringParam(parameterMap, PARAM_SEO_NAME);
    String format = getMandatoryStringParam(parameterMap, PARAM_FORMAT);

    String urlParams = parameterMap.entrySet().stream()
        .filter(entry -> ALLOWED_URL_PARAMS.contains(entry.getKey()) && entry.getValue() != null)
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));

    String assetId = DigestUtils.md5Hex(path);

    StringBuilder sb = new StringBuilder();
    sb.append(ASSET_DELIVERY_URL_PREFIX)
        .append("/").append(assetId)
        .append("/").append(seoname)
        .append(".").append(format);
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

}
