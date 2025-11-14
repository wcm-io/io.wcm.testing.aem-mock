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
package io.wcm.handler.url;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;

/**
 * Rewrites and builds URLs for links to content pages and resources.
 *
 * <p>
 * The interface is implemented by a Sling Model. You can adapt from
 * {@link org.apache.sling.api.SlingHttpServletRequest} or {@link org.apache.sling.api.resource.Resource} to get a
 * context-specific handler instance.
 * </p>
 */
@ProviderType
public interface UrlHandler {

  /**
   * Selector that is always added if a Sling-URL contains a suffix (to avoid files and directories with same name in
   * dispatcher cache)
   */
  @NotNull
  String SELECTOR_SUFFIX = "suffix";

  /**
   * Builds and optionally externalizes an URL using a builder pattern.
   * @param path Path to start URL building with
   * @return URL builder which allows to chain further optional parameters before building the URL string.
   */
  @NotNull
  UrlBuilder get(@NotNull String path);

  /**
   * Builds and optionally externalizes an URL using a builder pattern.
   * @param resource Resource, URL building is started with its path
   * @return URL builder which allows to chain further optional parameters before building the URL string.
   */
  @NotNull
  UrlBuilder get(@NotNull Resource resource);

  /**
   * Builds and optionally externalizes an URL using a builder pattern.
   * @param page Page Page, URL building is started with its path
   * @return URL builder which allows to chain further optional parameters before building the URL string.
   */
  @NotNull
  UrlBuilder get(@NotNull Page page);

  /**
   * Rewrites given path to current site or context.
   * The site root path is replaced with the one from current site
   * This is useful if a link to an internal page points to a page outside the site (e.g. because the page containing
   * the link was copied from the other site or inherited). When the AEM built-in rewrite logic was not applied the link
   * would be invalid. This methods rewrites the link path to the current site to try to resolve it there.
   * @param resource Resource to rewrite path from
   * @return Rewritten path or null if resource invalid
   */
  @Nullable
  String rewritePathToContext(@NotNull Resource resource);

  /**
   * Rewrites given path to given site or context.
   * The site root path is replaced with the one from current site.
   * This is useful if a link to an internal page points to a page outside the site (e.g. because the page containing
   * the link was copied from the other site or inherited). When the AEM built-in rewrite logic was not applied the link
   * would be invalid. This methods rewrites the link path to the current site to try to resolve it there.
   * @param resource Resource to rewrite path from
   * @param contextResource Context resource to which the path should be rewritten to
   * @return Rewritten path or null if resource or context resource is invalid
   */
  @Nullable
  String rewritePathToContext(@NotNull Resource resource, @NotNull Resource contextResource);

  /**
   * Checks if the given URL is externalized.
   *
   * <p>
   * An URL is treated as externalized if:
   * </p>
   *
   * <ul>
   * <li>It starts with a protocol and a colon (e.g. http:, tel:, mailto:, javascript:)</li>
   * <li>It starts with // or #</li>
   * </ul>
   * @param url URL
   * @return true if the URL is externalized.
   */
  boolean isExternalized(@NotNull String url);

  /**
   * Applies auto-detection of Site URL (author or publish instance) for given Site URL that is
   * configured in {@link SiteConfig}.
   *
   * <p>
   * If this Site URL contains an <code>&lt;auto&gt;</code> placeholder the Site URL detection is enabled
   * and the Site URL is replaced with the current hostname (if possible). Otherwise the remaining part of the
   * Site URL string is returned as fallback.
   * </p>
   *
   * <p>
   * Site URL auto-detection does only work in context of a request - outside request context the placeholders
   * is removed and the remaining string returned as fallback.
   * </p>
   *
   * @param siteUrl Site URL (author or publish) {@link SiteConfig}.
   * @return Automatic detected Site URL or fallback
   */
  String applySiteUrlAutoDetection(@Nullable String siteUrl);

}
