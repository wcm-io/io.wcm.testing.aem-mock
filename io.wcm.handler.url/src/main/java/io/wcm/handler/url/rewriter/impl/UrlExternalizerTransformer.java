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
package io.wcm.handler.url.rewriter.impl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import io.wcm.handler.url.UrlHandler;

/**
 * HTML transformer that rewrites URLs in certain HTML element attributes.
 */
class UrlExternalizerTransformer implements Transformer {

  private UrlExternalizerTransformerConfig transformerConfig;
  private ContentHandler contentHandler = EMPTY_CONTENT_HANDLER;
  private UrlHandler urlHandler;

  private static final Logger log = LoggerFactory.getLogger(UrlExternalizerTransformer.class.getName());
  private static final ContentHandler EMPTY_CONTENT_HANDLER = new DefaultHandler();

  @Override
  public void init(ProcessingContext pipelineContext, ProcessingComponentConfiguration config) {
    log.trace("Initialize UrlExternalizerTransformer with config: {}", config.getConfiguration());
    transformerConfig = new UrlExternalizerTransformerConfig(config.getConfiguration());
    urlHandler = pipelineContext.getRequest().adaptTo(UrlHandler.class);
  }

  @Override
  public void setContentHandler(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
  }

  @Override
  @SuppressWarnings("PMD.UseStringBufferForStringAppends")
  public void startElement(String nsUri, String name, String raw, Attributes attrs) throws SAXException {

    // check if for this element an attribute for rewriting is configured
    String rewriteAttr = transformerConfig.getElementAttributeNames().get(name);
    if (rewriteAttr == null) {
      log.trace("Rewrite element {}: Skip - No rewrite attribute configured.", name);
      contentHandler.startElement(nsUri, name, raw, attrs);
      return;
    }

    // validate URL handler
    if (urlHandler == null) {
      log.warn("Rewrite element {}: Skip - Unable to get URL handler/Integrator handler instance.", name);
      contentHandler.startElement(nsUri, name, raw, attrs);
      return;
    }

    // check if attribute exists
    int attributeIndex = attrs.getIndex(rewriteAttr);
    if (attributeIndex < 0) {
      log.trace("Rewrite element {}: Skip - Attribute does not exist: {}", name, rewriteAttr);
      contentHandler.startElement(nsUri, name, raw, attrs);
      return;
    }

    // rewrite URL
    String url = attrs.getValue(attributeIndex);
    if (StringUtils.isEmpty(url)) {
      log.trace("Rewrite element {}: Skip - URL is empty.", name);
      contentHandler.startElement(nsUri, name, raw, attrs);
      return;
    }

    // split off query string or fragment that may be appended to the URL
    String urlRemainder = null;
    int urlRemainderPos = StringUtils.indexOfAny(url, '?', '#');
    if (urlRemainderPos >= 0) {
      urlRemainder = url.substring(urlRemainderPos);
      url = url.substring(0, urlRemainderPos);
    }

    // decode URL (without URL remainder)
    url = URLDecoder.decode(url, StandardCharsets.UTF_8);

    // externalize URL (if it is not already externalized)
    String rewrittenUrl = urlHandler.get(url).buildExternalResourceUrl();
    if (urlRemainder != null) {
      if (rewrittenUrl == null) {
        rewrittenUrl = urlRemainder;
      }
      else {
        rewrittenUrl += urlRemainder;
      }
    }

    if (StringUtils.equals(url, rewrittenUrl)) {
      log.debug("Rewrite element {}: Skip - URL is already externalized: {}", name, url);
      contentHandler.startElement(nsUri, name, raw, attrs);
      return;
    }

    // set new attribute value
    log.debug("Rewrite element {}: Rewrite URL {} to {}", name, url, rewrittenUrl);
    AttributesImpl newAttrs = new AttributesImpl(attrs);
    newAttrs.setValue(attributeIndex, rewrittenUrl);
    contentHandler.startElement(nsUri, name, raw, newAttrs);
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    this.contentHandler.setDocumentLocator(locator);
  }

  @Override
  public void startDocument() throws SAXException {
    this.contentHandler.startDocument();
  }

  @Override
  public void endDocument() throws SAXException {
    this.contentHandler.endDocument();
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    this.contentHandler.startPrefixMapping(prefix, uri);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    this.contentHandler.endPrefixMapping(prefix);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    this.contentHandler.endElement(uri, localName, qName);
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    this.contentHandler.characters(ch, start, length);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    this.contentHandler.ignorableWhitespace(ch, start, length);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    this.contentHandler.processingInstruction(target, data);
  }

  @Override
  public void skippedEntity(String name) throws SAXException {
    this.contentHandler.skippedEntity(name);
  }

  @Override
  public void dispose() {
    // nothing to do
  }

}
