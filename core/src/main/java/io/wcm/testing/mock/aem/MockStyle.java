/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.wcm.api.designer.Cell;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Style;

/**
 * Mock implementation of {@link Style}.
 */
@SuppressWarnings("squid:S2160") // Not extending the equals implementation
class MockStyle extends ValueMapDecorator implements Style {

  private final Design design;

  /**
   * @param props Value map for style properties
   * @param design Design
   */
  MockStyle(@NotNull ValueMap props, @Nullable Design design) {
    super(props);
    this.design = design;
  }

  @Override
  public Design getDesign() {
    return design;
  }

  // --- unsupported operations ---

  @Override
  public Cell getCell() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDefiningPath(String arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Resource getDefiningResource(String arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Style getSubStyle(String arg0) {
    throw new UnsupportedOperationException();
  }

}
