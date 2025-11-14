/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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
package io.wcm.handler.url.impl;

import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements auto-detection for Site URL prefix (author or publish).
 * This works only if the adaptable is of SlingHttpServletRequest.
 */
final class UrlPrefix {

  /**
   * String that is provided in site URL config to enable auto-detection.
   */
  public static final String AUTO_DETECTION = "<auto>";

  static final String HTTP_HEADER_X_FORWARDED_HOST = "X-Forwarded-Host";
  static final String HTTP_HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
  static final String HTTP_HEADER_HOST = "Host";
  static final String HTTP_HEADER_X_FORWARDED_SSL = "X-Forwarded-SSL";
  static final String VALUE_ON = "on";

  private static final Logger log = LoggerFactory.getLogger(UrlPrefix.class);

  private UrlPrefix() {
    // static methods only
  }

  /**
   * Apply auto-detection to URL prefix.
   * @param configuredUrlPrefix Configured URL prefix
   * @param adaptable Adaptable
   * @return Configured or auto-detected URL prefix
   */
  static @Nullable String applyAutoDetection(@Nullable String configuredUrlPrefix, @NotNull Adaptable adaptable) {
    String urlPrefix = configuredUrlPrefix;
    if (StringUtils.contains(configuredUrlPrefix, AUTO_DETECTION)) {
      // remove auto marker (detection might not be possible if adaptable is not a request)
      urlPrefix = StringUtils.trimToNull(StringUtils.remove(configuredUrlPrefix, AUTO_DETECTION));
      // auto-detect based on request
      if (adaptable instanceof SlingHttpServletRequest) {
        SlingHttpServletRequest request = (SlingHttpServletRequest)adaptable;
        urlPrefix = detectFromForwardedHeader(request);
        if (urlPrefix == null) {
          urlPrefix = detectFromServletRequest(request);
        }
      }
    }
    return urlPrefix;
  }

  /**
   * Try to get URL prefix from X-Forwarded header (e.g. in AEMaaCS environment).
   * @param request Request
   * @return Url prefix or null
   */
  private static @Nullable String detectFromForwardedHeader(@NotNull SlingHttpServletRequest request) {

    // output request headers to log in TRACE level
    if (log.isTraceEnabled()) {
      StringBuilder output = new StringBuilder();
      Enumeration<String> headers = request.getHeaderNames();
      while (headers.hasMoreElements()) {
        String header = headers.nextElement();
        if (output.length() > 0) {
          output.append("; ");
        }
        output.append(header).append('=').append(request.getHeader(header));
      }
      log.trace("HTTP headers: {}", output);
    }

    // this should work for AEMaaCS author
    String forwardedHost = request.getHeader(HTTP_HEADER_X_FORWARDED_HOST);
    String forwardedProto = request.getHeader(HTTP_HEADER_X_FORWARDED_PROTO);
    if (StringUtils.isNotEmpty(forwardedHost) && StringUtils.isNotEmpty(forwardedProto)) {
      return forwardedProto + "://" + forwardedHost;
    }

    // this should work for AEMaaCS publish
    String host = request.getHeader(HTTP_HEADER_HOST);
    String forwardedSsl = request.getHeader(HTTP_HEADER_X_FORWARDED_SSL);
    if (StringUtils.isNotEmpty(host) && StringUtils.equalsIgnoreCase(forwardedSsl, VALUE_ON)) {
      return "https://" + host;
    }

    return null;
  }

  private static @NotNull String detectFromServletRequest(@NotNull SlingHttpServletRequest request) {
    StringBuilder urlPrefix = new StringBuilder();
    urlPrefix.append(request.getScheme()).append("://").append(request.getServerName());
    int port = request.getServerPort();
    if ((request.isSecure() && port != 443) || (!request.isSecure() && port != 80)) {
      urlPrefix.append(':').append(Integer.toString(port));
    }
    return urlPrefix.toString();
  }

}
