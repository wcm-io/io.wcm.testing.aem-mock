/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 - 2015 wcm.io
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

import static com.day.cq.tagging.TagConstants.TAG_ROOT_PATH;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.jcr.Session;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.RangeIterator;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagConstants;
import com.day.cq.tagging.TagException;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.NameConstants;

/**
 * Mock implementation of {@link TagManager}.
 */
@ProviderType
@SuppressWarnings("java:S112") // allow throwing RuntimException
public final class MockTagManager implements TagManager {

  /** resource type for created tags */
  private static final String TAG_RESOURCE_TYPE = "cq/tagging/components/tag";

  private final ResourceResolver resourceResolver;
  private final Logger log;

  MockTagManager(@NotNull ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
    log = LoggerFactory.getLogger(TagManager.class);

    // create some basic tag structure now, to avoid complications occurring later around this
    initTagsStructure();
  }

  /**
   * @return Tag root path.
   */
  public static String getTagRootPath() {
    return TAG_ROOT_PATH;
  }

  private void initTagsStructure() {
    Resource defaultNamespace = resourceResolver.getResource(getTagRootPath() + "/" + TagConstants.DEFAULT_NAMESPACE);
    // if it's already existing, then don't proceed any further
    if (defaultNamespace != null) {
      return;
    }
    Map<String, Object> etcProperties = new HashMap<>();
    etcProperties.put(JcrConstants.JCR_PRIMARYTYPE, "sling:Folder");

    Map<String, Object> tagsProperties = new HashMap<>();
    tagsProperties.put(JcrConstants.JCR_PRIMARYTYPE, "sling:Folder");
    tagsProperties.put(JcrConstants.JCR_TITLE, "Tags");
    // locale strings that are recognized languages in child tags
    tagsProperties.put("languages", new String[]{"en", "de", "es", "fr", "it", "pt_br", "zh_cn", "ch_tw", "ja", "ko_kr"});

    try {
      ResourceUtil.getOrCreateResource(resourceResolver, "/etc", etcProperties, null, true);
      ResourceUtil.getOrCreateResource(resourceResolver, getTagRootPath(), tagsProperties, null, true);
      createTag(TagConstants.DEFAULT_NAMESPACE_ID, "Standard Tags", null);
    }
    catch (PersistenceException | InvalidTagFormatException e) {
      log.error("Error creating tags tree", e);
    }
  }

  private String getPathFromID(String tagID) throws InvalidTagFormatException {
    if (tagID == null) {
      throw new InvalidTagFormatException("tagID is null");
    }
    if (StringUtils.startsWith(tagID, getTagRootPath())) {
      // absolute path mode
      if (!tagID.startsWith(getTagRootPath())) {
        // TODO: seems reasonable, but is it worth enforcing?
        throw new InvalidTagFormatException("Tags are only allowed to be under " + getTagRootPath());
      }
      return tagID;
    }
    else if (tagID.contains(TagConstants.NAMESPACE_DELIMITER)) {
      // namespace mode
      String tagPath = tagID.replaceFirst(TagConstants.NAMESPACE_DELIMITER, "/");
      if (tagPath.contains(TagConstants.NAMESPACE_DELIMITER)) {
        throw new InvalidTagFormatException("tag ID contains multiple namespace declarations");
      }
      // remove any possible trailing slashes, such as the namespace only case
      if (tagPath.endsWith("/")) {
        tagPath = tagPath.substring(0, tagPath.length() - 1);
      }
      return getTagRootPath() + "/" + tagPath;
    }
    else {
      // default namespace mode
      return getTagRootPath() + "/" + TagConstants.DEFAULT_NAMESPACE + "/" + tagID;
    }
  }

  @Override
  public boolean canCreateTag(String tagID) throws InvalidTagFormatException {
    String tagPath = getPathFromID(tagID);
    return resourceResolver.getResource(tagPath) == null;
  }

  @Override
  public Tag createTag(String tagID, String title, String description)
      throws InvalidTagFormatException {
    return createTag(tagID, title, description, true);
  }

  @Override
  public Tag createTag(String tagID, String title, String description, boolean autoSave)
      throws InvalidTagFormatException {
    String tagPath = getPathFromID(tagID);
    if (!StringUtils.startsWith(tagPath, TAG_ROOT_PATH)) {
      throw new InvalidTagFormatException("Tag path '" + tagPath + "' does not start with: " + TAG_ROOT_PATH);
    }

    Resource tagResource = resourceResolver.getResource(tagPath);
    if (tagResource != null) {
      return tagResource.adaptTo(Tag.class);
    }

    // ensure the parent exists first
    String parentTagPath = tagPath.substring(0, tagPath.lastIndexOf('/'));
    if (!getTagRootPath().equals(parentTagPath)) {
      createTag(parentTagPath, null, null, false);
    }

    // otherwise it needs to be made
    Map<String, Object> tagProps = new HashMap<>();
    tagProps.put(JcrConstants.JCR_PRIMARYTYPE, TagConstants.NT_TAG);
    tagProps.put(ResourceResolver.PROPERTY_RESOURCE_TYPE, TAG_RESOURCE_TYPE);
    if (title != null) {
      tagProps.put(JcrConstants.JCR_TITLE, title);
    }
    if (description != null) {
      tagProps.put(JcrConstants.JCR_DESCRIPTION, description);
    }
    tagProps.put(NameConstants.PN_LAST_MOD, Calendar.getInstance());
    tagProps.put(NameConstants.PN_LAST_MOD_BY, resourceResolver.getUserID());

    try {
      tagResource = ResourceUtil.getOrCreateResource(resourceResolver, tagPath, tagProps, null, autoSave);

      return tagResource.adaptTo(Tag.class);
    }
    catch (PersistenceException ex) {
      throw new RuntimeException("failed to create tag", ex);
    }
  }

  @Override
  public Tag createTagByTitle(String titlePath) throws InvalidTagFormatException {
    return createTagByTitle(titlePath, true);
  }

  @Override
  public void deleteTag(Tag tag) {
    deleteTag(tag, true);
  }

  @Override
  public void deleteTag(Tag tag, boolean autoSave) {
    Resource tagResource = tag.adaptTo(Resource.class);
    if (tagResource == null) {
      return;
    }
    try {
      resourceResolver.delete(tagResource);
      if (autoSave) {
        resourceResolver.commit();
        resourceResolver.refresh();
      }
    } catch (PersistenceException e) {
      log.error("error deleting tag", e);
    }
  }

  @Override
  public RangeIterator<Resource> find(String tagID) {
    return find("/", new String[] {
        tagID
    }, false);
  }

  @Override
  public RangeIterator<Resource> find(String basePath, String[] tagIDs) {
    return find(basePath, tagIDs, false);
  }

  @Override
  public RangeIterator<Resource> find(String basePath, String[] tagIDs, boolean oneMatchIsEnough) {
    Resource base = resourceResolver.getResource(basePath);
    if (base == null) {
      return new CollectionRangeIterator<>(Collections.<Resource>emptyList());
    }

    Collection<String> tagPaths = new HashSet<>(tagIDs.length);
    for (String tagID : tagIDs) {
      Tag tag = resolve(tagID);
      // clause - if tag does not exist, should return null.
      if (tag == null) {
        return null;
      }
      Resource tagResource = tag.adaptTo(Resource.class);
      if (tagResource != null) {
        tagPaths.add(tagResource.getPath());
      }
    }

    Queue<Resource> searchResources = new LinkedList<>();
    searchResources.add(base);

    Collection<Resource> matchedResources = new ArrayList<>();

    while (!searchResources.isEmpty()) {
      Resource resource = searchResources.poll();
      // add the children to search the entire tree
      CollectionUtils.addAll(searchResources, resource.listChildren());

      // now process the tags
      String[] resourceTags = resource.getValueMap().get(TagConstants.PN_TAGS, String[].class);
      if (resourceTags == null) {
        continue;
      }

      List<String> resourceTagPaths = new ArrayList<>(resourceTags.length);
      try {
        for (String resourceTag : resourceTags) {
          resourceTagPaths.add(getPathFromID(resourceTag));
        }
      } catch (InvalidTagFormatException e) {
        log.error("invalid tag id encountered", e);
      }

      if (resourceTagPaths.isEmpty()) {
        continue;
      }

      boolean matches = false;
      if (oneMatchIsEnough) {
        // this is essentially an OR list, so break out on the first positive
        oneMatched:
          for (String tagPath : tagPaths) {
            for (String resourceTagPath : resourceTagPaths) {
              matches = doTagsMatch(resourceTagPath, tagPath);
              if (matches) {
                break oneMatched;
              }
            }
          }
      } else {
        // this is essentially an AND list, so break out on the first failure
        matches = true;
        for (String tagPath : tagPaths) {
          boolean tagMatched = false;
          for (Iterator<String> resourceTagPathIter = resourceTagPaths.iterator(); !tagMatched && resourceTagPathIter.hasNext();) {
            String resourceTagPath = resourceTagPathIter.next();
            tagMatched = doTagsMatch(resourceTagPath, tagPath);
          }
          // if no tag on the resource matched the current search tag, it fails the search
          if (!tagMatched) {
            matches = false;
            break;
          }
        }
      }

      if (matches) {
        matchedResources.add(resource);
      }
    }

    return new CollectionRangeIterator<>(matchedResources);
  }

  /**
   * Test matching of tags. <em>matching</em> is defined as either being equivalent to or starting with.
   * @param haystack the tag (absolute path) to be tested as matching the <code>needle</code> tag.
   * @param needle the tag (absolute path) to verify the <code>haystack</code> as matching.
   * @return state of <code>haystack</code> tag matching the <code>needle</code> tag.
   */
  private boolean doTagsMatch(String haystack, String needle) {
    // clause - sub tags are included when searching for a parent tag
    return haystack.equals(needle) || haystack.startsWith(needle + "/");
  }

  private List<Tag> getNamespacesList() {
    List<Tag> namespaces = new ArrayList<>();
    Resource tagRoot = resourceResolver.getResource(getTagRootPath());
    if (tagRoot != null) {
      for (Iterator<Resource> resources = tagRoot.listChildren(); resources.hasNext();) {
        Resource resource = resources.next();
        Tag tag = resource.adaptTo(Tag.class);
        if (tag != null) {
          namespaces.add(tag);
        }
      }
    }
    return namespaces;
  }

  @Override
  public Tag[] getNamespaces() {
    List<Tag> namespaces = getNamespacesList();
    return namespaces.toArray(new Tag[0]);
  }

  @Override
  public Iterator<Tag> getNamespacesIter() {
    return getNamespacesList().iterator();
  }

  @Override
  public Session getSession() {
    return resourceResolver.adaptTo(Session.class);
  }

  @Override
  public Tag[] getTags(Resource resource) {
    return getTagsForSubtree(resource, true);
  }

  @Override
  public Tag[] getTagsForSubtree(Resource resource, boolean shallow) {
    Collection<Tag> tags = collectResourceTags(resource, !shallow);
    return tags.toArray(new Tag[0]);
  }

  private Collection<Tag> collectResourceTags(Resource resource, boolean recurse) {
    if (resource == null) {
      return Collections.emptyList();
    }
    Set<Tag> treeTags = new LinkedHashSet<>();
    Queue<Resource> searchResources = new LinkedList<>();
    searchResources.add(resource);

    while (!searchResources.isEmpty()) {
      Resource searchResource = searchResources.poll();

      if (recurse) {
        CollectionUtils.addAll(searchResources, searchResource.listChildren());
      }

      String[] tags = searchResource.getValueMap().get(TagConstants.PN_TAGS, String[].class);
      if (tags == null) {
        continue;
      }

      for (String tagStr : tags) {
        Tag tag = resolve(tagStr);
        if (tag != null) {
          treeTags.add(tag);
        }
      }
    }
    return treeTags;
  }

  @Override
  public Tag resolve(String tagID) {
    try {
      String path = getPathFromID(tagID);
      Resource tagResource = resourceResolver.getResource(path);
      if (tagResource != null) {
        return tagResource.adaptTo(Tag.class);
      }
    }
    catch (InvalidTagFormatException e) {
      // ignore
    }
    return null;
  }

  @Override
  public void setTags(Resource resource, Tag[] tags) {
    setTags(resource, tags, true);
  }

  @Override
  public void setTags(Resource resource, Tag[] tags, boolean autoSave) {
    ModifiableValueMap props = resource.adaptTo(ModifiableValueMap.class);
    if (props == null) {
      throw new IllegalStateException("Unable to get modifiable value map: " + resource.getPath());
    }
    if (tags == null) {
      props.remove(TagConstants.PN_TAGS);
    }
    else {
      String[] tagStrings = new String[tags.length];
      for (int i = 0; i < tags.length; ++i) {
        // 6.0 has appeared to have switched to storing (the shorter) tagIDs, from where 5.6 was storing absolute paths.
        tagStrings[i] = tags[i].getTagID();
      }
      props.put(TagConstants.PN_TAGS, tagStrings);
    }

    if (autoSave) {
      try {
        resourceResolver.commit();
      }
      catch (PersistenceException e) {
        log.error("failed to commit updates for setting tags", e);
      }
    }
  }

  @Override
  public ResourceResolver getResourceResolver() {
    return resourceResolver;
  }


  // --- unsupported operations ---
  //CHECKSTYLE:OFF

  @Override
  public boolean canCreateTagByTitle(String tagTitlePath) throws InvalidTagFormatException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canCreateTagByTitle(String tagTitlePath, Locale locale) throws InvalidTagFormatException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tag createTagByTitle(String titlePath, boolean autoSave) throws InvalidTagFormatException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tag createTagByTitle(String titlePath, Locale locale) throws InvalidTagFormatException {
    throw new UnsupportedOperationException();
  }

  @Override
  public RangeIterator<Resource> find(String basePath, List<String[]> tagSetIDs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FindResults findByTitle(String title) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void mergeTag(Tag tag, Tag destination) throws TagException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tag moveTag(Tag tag, String destination) throws InvalidTagFormatException, TagException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tag resolveByTitle(String tagTitlePath) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tag resolveByTitle(String tagTitlePath, Locale locale) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Tag[] findTagsByTitle(String keyword, Locale locale) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterable<Tag> findTagsByKeyword(String arg0, Locale arg1, String arg2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getSupportedLanguageCodes() {
    throw new UnsupportedOperationException();
  }

}
