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

import static io.wcm.handler.url.impl.UrlPrefix.HTTP_HEADER_HOST;
import static io.wcm.handler.url.impl.UrlPrefix.HTTP_HEADER_X_FORWARDED_HOST;
import static io.wcm.handler.url.impl.UrlPrefix.HTTP_HEADER_X_FORWARDED_PROTO;
import static io.wcm.handler.url.impl.UrlPrefix.HTTP_HEADER_X_FORWARDED_SSL;
import static io.wcm.handler.url.impl.UrlPrefix.VALUE_ON;
import static io.wcm.handler.url.impl.UrlPrefix.applyAutoDetection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.url.UrlHandler;
import io.wcm.handler.url.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class UrlPrefixTest {

  final AemContext context = AppAemContext.newAemContext();
  private Resource resource;

  @BeforeEach
  void setUp() {
    resource = context.currentResource(context.create().resource("/content/test"));
    context.request().setServerName("servername");
    context.request().setServerPort(8080);
  }

  @Test
  void testResourceNull() {
    assertNull(applyAutoDetection(null, resource));
  }

  @Test
  void testResourceEmpty() {
    assertEquals("", applyAutoDetection("", resource));
  }

  @Test
  void testResourceUrl() {
    assertEquals("https://myhost", applyAutoDetection("https://myhost", resource));
  }

  @Test
  void testResourcePlaceholder() {
    assertNull(applyAutoDetection("<auto>", resource));
  }

  @Test
  void testResourceUrlPlaceholder() {
    assertEquals("https://myhost", applyAutoDetection("<auto>https://myhost", resource));
  }

  @Test
  void testRequestNull() {
    assertNull(applyAutoDetection(null, context.request()));
  }

  @Test
  void testRequestEmpty() {
    assertEquals("", applyAutoDetection("", context.request()));
  }

  @Test
  void testRequestUrl() {
    assertEquals("https://myhost", applyAutoDetection("https://myhost", context.request()));
  }

  @Test
  void testRequestPlaceholder() {
    assertEquals("http://servername:8080", applyAutoDetection("<auto>", context.request()));
  }

  @Test
  void testRequestPlaceholderHttps() {
    context.request().setServerPort(8443);
    context.request().setScheme("https");
    assertEquals("https://servername:8443", applyAutoDetection("<auto>", context.request()));
  }

  @Test
  void testRequestPlaceholderHttpDefaultPort() {
    context.request().setServerPort(80);
    context.request().setScheme("http");
    assertEquals("http://servername", applyAutoDetection("<auto>", context.request()));
  }

  @Test
  void testRequestPlaceholderHttpsDefaultPort() {
    context.request().setServerPort(443);
    context.request().setScheme("https");
    assertEquals("https://servername", applyAutoDetection("<auto>", context.request()));
  }

  @Test
  void testRequestUrlPlaceholder() {
    assertEquals("http://servername:8080", applyAutoDetection("<auto>https://myhost", context.request()));
  }

  @Test
  void testRequestUrlPlaceholder_URLHandler() {
    UrlHandler urlHandler = AdaptTo.notNull(context.request(), UrlHandler.class);
    assertEquals("http://servername:8080", urlHandler.applySiteUrlAutoDetection("<auto>https://myhost"));
  }

  @Test
  void testRequestPlaceholder_AEMaaCS_author() {
    context.request().setHeader(HTTP_HEADER_X_FORWARDED_HOST, "aemaacs-author");
    context.request().setHeader(HTTP_HEADER_X_FORWARDED_PROTO, "https");
    assertEquals("https://aemaacs-author", applyAutoDetection("<auto>", context.request()));
  }

  @Test
  void testRequestPlaceholder_AEMaaCS_publish() {
    context.request().setHeader(HTTP_HEADER_HOST, "aemaacs-publish");
    context.request().setHeader(HTTP_HEADER_X_FORWARDED_SSL, VALUE_ON);
    assertEquals("https://aemaacs-publish", applyAutoDetection("<auto>", context.request()));
  }

  @Test
  void testRequestPlaceholder_incomplete1() {
    context.request().setHeader(HTTP_HEADER_X_FORWARDED_HOST, "aemaacs-author");
    assertEquals("http://servername:8080", applyAutoDetection("<auto>", context.request()));
  }

  @Test
  void testRequestPlaceholder_incomplete2() {
    context.request().setHeader(HTTP_HEADER_X_FORWARDED_PROTO, "https");
    assertEquals("http://servername:8080", applyAutoDetection("<auto>", context.request()));
  }

  @Test
  void testRequestPlaceholder_incomplete3() {
    context.request().setHeader(HTTP_HEADER_HOST, "aemaacs-publish");
    assertEquals("http://servername:8080", applyAutoDetection("<auto>", context.request()));
  }

  @Test
  void testRequestPlaceholder_incomplete4() {
    context.request().setHeader(HTTP_HEADER_X_FORWARDED_SSL, VALUE_ON);
    assertEquals("http://servername:8080", applyAutoDetection("<auto>", context.request()));
  }

  @Test
  void testRequestPlaceholder_incomplete5() {
    context.request().setHeader(HTTP_HEADER_HOST, "aemaacs-publish");
    context.request().setHeader(HTTP_HEADER_X_FORWARDED_SSL, "off");
    assertEquals("http://servername:8080", applyAutoDetection("<auto>", context.request()));
  }

}
