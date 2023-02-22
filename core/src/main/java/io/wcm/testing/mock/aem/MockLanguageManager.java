/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package io.wcm.testing.mock.aem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.annotations.Component;

import com.day.cq.commons.Language;
import com.day.cq.commons.LanguageUtil;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.LanguageManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.text.Text;

/**
 * Mock implementation of {@link LanguageManager}.
 */
@Component(service = LanguageManager.class)
@ProviderType
public final class MockLanguageManager implements LanguageManager {

  @Override
  @Deprecated
  public Map<Locale, Info> getAdjacentInfo(final ResourceResolver resourceResolver, final String path) {
    return Optional.ofNullable(getAdjacentLanguageInfo(resourceResolver, path))
        .map(Map::entrySet)
        .map(Collection::stream)
        .map(entries -> entries.collect(toLinkedMap(i -> i.getKey().getLocale(), Map.Entry::getValue)))
        .orElse(null);
  }

  @Override
  public Map<Language, Info> getAdjacentLanguageInfo(final ResourceResolver resourceResolver, final String path) {
    return Optional.ofNullable(LanguageUtil.getLanguageRoot(path))
        .map(root -> path.substring(root.length()))
        .map(relPath -> relPath.startsWith("/") ? relPath.substring(1) : relPath)
        .map(relPath -> this.getLanguageRootStream(resourceResolver, path)
            .map(info -> info.getChild(relPath, resourceResolver))
            .collect(toLinkedMap(InfoImpl::getLanguage, i -> (Info)i)))
        .orElse(null);
  }

  @Override
  public Locale getLanguage(final Resource resource) {
    return this.getLanguage(resource, true);
  }

  @Override
  public Language getCqLanguage(final Resource resource) {
    return this.getCqLanguage(resource, true);
  }

  @Override
  public Locale getLanguage(final Resource resource, final boolean respectContent) {
    return Optional.ofNullable(getCqLanguage(resource, respectContent))
        .map(Language::getLocale)
        .orElse(null);
  }

  @Override
  @SuppressWarnings("null")
  public Language getCqLanguage(final Resource resource, final boolean respectContent) {
    Optional<Page> page = Optional.ofNullable(resource.getResourceResolver().adaptTo(PageManager.class))
        .map(pm -> pm.getContainingPage(resource));

    if (respectContent) {
      return page
          .map(Page::getContentResource)
          .map(HierarchyNodeInheritanceValueMap::new)
          .map(vm -> vm.getInherited(JcrConstants.JCR_LANGUAGE, String.class))
          .map(LanguageUtil::getLanguage)
          .orElseGet(() -> this.getCqLanguage(resource, false));
    }

    return page
        .map(Page::getPath)
        .map(LanguageUtil::getLanguageRoot)
        .map(Text::getName)
        .map(Language::new)
        .orElse(null);
  }

  @Override
  public Collection<Locale> getLanguages(final ResourceResolver resourceResolver, final String path) {
    return this.getCqLanguages(resourceResolver, path).stream()
        .map(Language::getLocale)
        .collect(Collectors.toList());
  }

  @Override
  public Collection<Language> getCqLanguages(final ResourceResolver resourceResolver, final String path) {
    return this.getLanguageRootStream(resourceResolver, path)
        .map(InfoImpl::getLanguage)
        .collect(Collectors.toList());
  }

  @Override
  @SuppressWarnings("null")
  public Collection<Page> getLanguageRoots(final ResourceResolver resourceResolver, final String path) {
    return this.getLanguageRootStream(resourceResolver, path)
        .map(InfoImpl::getResource)
        .filter(Objects::nonNull)
        .map(res -> res.adaptTo(Page.class))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  @SuppressWarnings({ "null"})
  public Page getLanguageRoot(final Resource resource) {
    return getLanguageRoot(resource, false);
  }

  @Override
  public @Nullable Page getLanguageRoot(Resource res, boolean respectContent) {
    Resource languageRootResource = getLanguageRootResource(res, respectContent);
    return (languageRootResource != null) ?
            languageRootResource.adaptTo(Page.class) :
            null;
  }

  @Override
  public Resource getLanguageRootResource(Resource res) {
    return getLanguageRootResource(res, false);
  }

  @Override
  public Resource getLanguageRootResource(Resource res, boolean respectContent) {
    String rootPath = getLanguageRootPath(res, respectContent);
    if (rootPath == null) {
      return null;
    }
    return res.getResourceResolver().getResource(rootPath);
  }

  @Override
  public Collection<Resource> getLanguageRootResources(ResourceResolver resolver, String path) {
    Iterator<Resource> resources = getLanguageRootSiblings(resolver, path, false);
    if (resources == null) {
      return Collections.emptySet();
    }
    List<Resource> roots = new ArrayList<>();
    while (resources.hasNext()) {
      Resource res = resources.next();
      Locale locale = LanguageUtil.getLocale(Text.getName(res.getPath()));
      if (locale != null) {
        roots.add(res);
      }
    }
    return roots;
  }

  @Override
  public Collection<Resource> getLanguageRootResources(ResourceResolver resolver, String path, boolean respectContent) {
    Iterator<Resource> siblings = getLanguageRootSiblings(resolver, path, respectContent);
    if (siblings == null) {
      return Collections.emptySet();
    }
    List<Resource> roots = new ArrayList<>();
    boolean additionalLanguageRootsFound = false;
    while (siblings.hasNext()) {
      Resource sibling = siblings.next();
      String locale = getLanguageRootLocale(sibling, respectContent);
      if (locale != null) {
        roots.add(sibling);
        continue;
      }
      additionalLanguageRootsFound |= addLanguageRootsFromChildren(roots, sibling, respectContent);
    }
    if (additionalLanguageRootsFound) {
      return roots;
    }
    Resource currentResource = resolver.getResource(path);
    if (currentResource != null) {
      Resource langRoot = getLanguageRootResource(currentResource, true);
      if (langRoot != null) {
        Resource langRootParent = langRoot.getParent();
        if (langRootParent != null) {
          Resource langRootGrandParent = langRootParent.getParent();
          ArrayList<Resource> nonLangRootUncles = new ArrayList<>();
          if (langRootGrandParent != null) {
            Iterator<Resource> langRootUncles = langRootGrandParent.listChildren();
            while (langRootUncles.hasNext()) {
              Resource langRootUncle = langRootUncles.next();
              if (langRootUncle.getName().equals(langRootParent.getName())) {
                continue;
              }
              String gcLocale = getLanguageRootLocale(langRootUncle, respectContent);
              if (gcLocale != null && !isCountryNode(langRootUncle, respectContent)) {
                roots.add(langRootUncle);
                additionalLanguageRootsFound = true;
                continue;
              }
              nonLangRootUncles.add(langRootUncle);
            }
          }
          if (!additionalLanguageRootsFound) {
            return roots;
          }
          for (Resource nonLangRootUncle : nonLangRootUncles) {
            addLanguageRootsFromChildren(roots, nonLangRootUncle, respectContent);
          }
        }
      }
    }
    return roots;
  }

  private String getLanguageRootPath(Resource res, boolean respectContent) {
    String path = res.getPath();
    if (respectContent) {
      int idx = path.indexOf("/jcr:content");
      if (idx > 0) {
        path = path.substring(0, idx);
      }
      Resource hr = res.getResourceResolver().getResource(path);
      while (hr != null && !hr.getPath().equals("/")) {
        ValueMap props = hr.getValueMap();
        if (props.get("jcr:content/cq:isLanguageRoot", Boolean.FALSE)) {
          String iso = props.get("jcr:content/jcr:language", "");
          Language locale = iso.isEmpty() ? null : LanguageUtil.getLanguage(iso);
          if (locale != null) {
            return hr.getPath();
          }
        }
        hr = hr.getParent();
      }
    }
    return LanguageUtil.getLanguageRoot(path);
  }

  @Nullable
  private Iterator<Resource> getLanguageRootSiblings(ResourceResolver resolver, String path, boolean respectContent) {
    if (path == null) {
      return null;
    }
    Resource res = resolver.getResource(path);
    if (res == null) {
      return null;
    }
    String root = getLanguageRootPath(res, respectContent);
    if (root == null) {
      return null;
    }
    String parent = Text.getRelativeParent(root, 1);
    Resource parentResource = resolver.getResource(parent);
    if (parentResource == null) {
      return null;
    }
    return resolver.listChildren(parentResource);
  }

  private boolean isCountryNode(Resource resource, boolean respectContent) {
    Iterator<Resource> children = resource.listChildren();
    while (children.hasNext()) {
      Resource child = children.next();
      String gcLocale = getLanguageRootLocale(child, respectContent);
      if (gcLocale != null) {
        return true;
      }
    }
    return false;
  }

  private boolean addLanguageRootsFromChildren(List<Resource> roots, Resource resource, boolean respectContent) {
    Iterator<Resource> children = resource.listChildren();
    boolean additionalLanguageRootsFound = false;
    while (children.hasNext()) {
      Resource child = children.next();
      String childLocale = getLanguageRootLocale(child, respectContent);
      if (childLocale != null) {
        roots.add(child);
        additionalLanguageRootsFound = true;
      }
    }
    return additionalLanguageRootsFound;
  }

  private String getLanguageRootLocale(Resource res, boolean respectContent) {
    if (null == res) {
      return null;
    }
    if (respectContent && !StringUtils.equals(res.getPath(), "/")) {
      ValueMap props = res.getValueMap();
      if (props.get("jcr:content/cq:isLanguageRoot", Boolean.FALSE)) {
        String iso = props.get("jcr:content/jcr:language", "");
        Language language1 = iso.isEmpty() ? null : LanguageUtil.getLanguage(iso);
        if (language1 != null) {
          return language1.getLocale().toString();
        }
      }
    }
    Language language = LanguageUtil.getLanguage(res.getName());
    if (language != null) {
      return res.getName();
    }
    return null;
  }

  @SuppressWarnings("null")
  private Stream<InfoImpl> getLanguageRootStream(final ResourceResolver resourceResolver, final String path) {
    return Optional.ofNullable(LanguageUtil.getLanguageRoot(path))
        .map(resourceResolver::getResource)
        .map(Resource::getParent)
        .map(Resource::listChildren)
        .map(childIterator -> StreamSupport.stream(((Iterable<Resource>)() -> childIterator).spliterator(), false))
        .orElseGet(Stream::empty)
        .filter(res -> Objects.nonNull(LanguageUtil.getLanguage(res.getName())))
        .map(res -> new InfoImpl(res.getPath(), res, LanguageUtil.getLanguage(res.getName())));
  }

  /**
   * Collector for collecting a stream to a linked hash map.
   * @param keyMapper A mapping function to produce keys.
   * @param valueMapper A mapping function to produce values.
   * @param <T> The type of input elements to the reduction operation.
   * @param <K> The output type of the key mapping function.
   * @param <U> The output type of the value mapping function.
   * @return A Collector which collects elements into a LinkedHashMap whose keys are the result of applying a key
   *         mapping function to the input elements, and whose values are the result of applying a value mapping
   *         function to
   *         all input elements equal to the key and combining them using the merge function
   */
  private static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
      final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends U> valueMapper) {
    return Collectors.toMap(
        keyMapper,
        valueMapper,
        (u, v) -> {
          throw new IllegalStateException(String.format("Duplicate key %s", u));
        },
        LinkedHashMap::new);
  }

  // --- unsupported operations ---

  @Override
  public String getIsoCountry(final Locale locale) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tree compareLanguageTrees(final ResourceResolver resourceResolver, final String path) {
    throw new UnsupportedOperationException();
  }


  private static final class InfoImpl implements LanguageManager.Info {

    private final String path;
    private final Resource resource;
    private final Language language;

    InfoImpl(@NotNull final String path, @Nullable final Resource resource, @NotNull final Language language) {
      this.path = path;
      this.resource = resource;
      this.language = language;
    }

    @Override
    public String getPath() {
      return this.path;
    }

    @Override
    public boolean exists() {
      return this.resource != null;
    }

    @Override
    public boolean hasContent() {
      return Optional.ofNullable(this.resource)
          .map(res -> resource.getChild(JcrConstants.JCR_CONTENT))
          .isPresent();
    }

    @Override
    @SuppressWarnings("null")
    public long getLastModified() {
      return Optional.ofNullable(this.resource)
          .map(res -> resource.getChild(JcrConstants.JCR_CONTENT))
          .map(Resource::getValueMap)
          .map(vm -> vm.get(JcrConstants.JCR_LASTMODIFIED, Long.class))
          .orElse(0L);
    }

    /**
     * The resource located at {@link #getPath()}, if it exists.
     * @return The resource.
     */
    @Nullable
    private Resource getResource() {
      return this.resource;
    }

    /**
     * Get the language.
     * @return The language.
     */
    @NotNull
    private Language getLanguage() {
      return this.language;
    }

    /**
     * Gets the InfoImpl for a child resource under the current InfoImpl's path.
     * <p>
     * This constructs a new InfoImpl using the path getPath() + / + relPath.
     * </p>
     * @param relPath Path relative to the current path.
     * @param resourceResolver A resource resolver.
     * @return A new InfoImpl for the resource specified at relPath.
     */
    private InfoImpl getChild(@NotNull final String relPath, @NotNull final ResourceResolver resourceResolver) {
      if (relPath.isEmpty()) {
        return this;
      }
      String childPath = String.join("/", this.path, relPath);
      Resource child = resourceResolver.getResource(childPath);
      return new InfoImpl(childPath, child, this.getLanguage());
    }
  }

}

