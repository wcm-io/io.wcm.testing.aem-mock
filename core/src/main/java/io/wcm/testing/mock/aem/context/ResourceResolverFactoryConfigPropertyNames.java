/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2023 wcm.io
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
package io.wcm.testing.mock.aem.context;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.sling.resourceresolver.impl.ResourceResolverFactoryConfig;

/**
 * The names of the vanity path allow/denylist configuration property changed between releases (SLING-11742).
 * Auto-detect the correct name for the resource resolver in current classpath.
 */
final class ResourceResolverFactoryConfigPropertyNames {

  private static final String VANITY_PATH_ALLOW_LIST_PROPERTY_NAME;
  private static final String VANITY_PATH_DENY_LIST_PROPERTY_NAME;

  static {
    // old names as fallback
    String vanityPathAllowListPropertyName = "resource.resolver.vanitypath.whitelist";
    String vanityPathDenyListPropertyName = "resource.resolver.vanitypath.blacklist";

    try {
      Class<?> resourceResolverFactoryConfigClass = Class.forName(ResourceResolverFactoryConfig.class.getName());
      Set<String> methodNames = Stream.of(resourceResolverFactoryConfigClass.getDeclaredMethods())
          .map(method -> method.getName())
          .collect(Collectors.toSet());
      // use new names as fields do exist
      if (methodNames.contains("resource_resolver_vanitypath_allowlist")
          && methodNames.contains("resource_resolver_vanitypath_denylist")) {
        vanityPathAllowListPropertyName = "resource.resolver.vanitypath.allowlist";
        vanityPathDenyListPropertyName = "resource.resolver.vanitypath.denylist";
      }
    }
    catch (ClassNotFoundException ex) {
      // ignore, keep old names
    }

    VANITY_PATH_ALLOW_LIST_PROPERTY_NAME = vanityPathAllowListPropertyName;
    VANITY_PATH_DENY_LIST_PROPERTY_NAME = vanityPathDenyListPropertyName;
  }

  static String getVanityPathAllowListPropertyName() {
    return VANITY_PATH_ALLOW_LIST_PROPERTY_NAME;
  }

  static String getVanityPathDenyListPropertyName() {
    return VANITY_PATH_DENY_LIST_PROPERTY_NAME;
  }

  private ResourceResolverFactoryConfigPropertyNames() {
    // static methods only
  }

}
